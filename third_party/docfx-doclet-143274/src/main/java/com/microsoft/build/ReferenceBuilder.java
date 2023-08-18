/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.getJavaSpec;
import static com.microsoft.build.BuilderUtil.populateItemFields;
import static com.microsoft.build.BuilderUtil.replaceUidAndSplit;
import static com.microsoft.build.BuilderUtil.splitUidWithGenericsIntoClassNames;

import com.microsoft.lookup.ClassLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.SpecViewModel;
import com.microsoft.util.ElementUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;

class ReferenceBuilder {
  private final Pattern JAVA_PATTERN = Pattern.compile("^java.*");
  private final Pattern PROTOBUF_PATTERN = Pattern.compile("^com.google.protobuf.*");
  private final Pattern GAX_PATTERN = Pattern.compile("^com.google.api.gax.*");
  private final Pattern APICOMMON_PATTERN = Pattern.compile("^com.google.api.core.*");
  private final Pattern LONGRUNNING_PATTERN = Pattern.compile("^com.google.longrunning.*");
  private final Pattern ENDING_PATTERN = Pattern.compile(".*<\\?>$");
  private final String PRIMITIVE_URL =
      "https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html";
  private final String JAVA_BASE_URL = "https://docs.oracle.com/javase/8/docs/api/";
  private final DocletEnvironment environment;
  private final ClassLookup classLookup;
  private final ElementUtil elementUtil;

  ReferenceBuilder(
      DocletEnvironment environment, ClassLookup classLookup, ElementUtil elementUtil) {
    this.environment = environment;
    this.classLookup = classLookup;
    this.elementUtil = elementUtil;
  }

  MetadataFileItem buildClassReference(TypeElement classElement) {
    MetadataFileItem referenceItem = new MetadataFileItem(classLookup.extractUid(classElement));
    referenceItem.setName(classLookup.extractName(classElement));
    referenceItem.setNameWithType(classLookup.extractNameWithType(classElement));
    referenceItem.setFullName(classLookup.extractFullName(classElement));
    return referenceItem;
  }

  void addReferencesInfo(TypeElement classElement, MetadataFile classMetadataFile) {
    MetadataFileItem classReference = new MetadataFileItem(classLookup.extractUid(classElement));
    classReference.setParent(classLookup.extractParent(classElement));
    populateItemFields(classReference, classLookup, classElement);
    classReference.setTypeParameters(classLookup.extractTypeParameters(classElement));

    addTypeParameterReferences(classReference, classMetadataFile);
    addSuperclassAndInterfacesReferences(classElement, classMetadataFile);
    addInnerClassesReferences(classElement, classMetadataFile);
  }

  void addChildrenReferences(
      Element element, List<String> packageChildren, Set<MetadataFileItem> referencesCollector) {
    for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
      referencesCollector.add(buildClassReference(classElement));

      packageChildren.add(classLookup.extractUid(classElement));
      addChildrenReferences(classElement, packageChildren, referencesCollector);
    }
  }

  String getJavaReferenceHref(String uid) {
    if (uid == null || uid.equals("")) {
      return JAVA_BASE_URL;
    }
    //  example1 uid: "java.lang.Object.equals(java.lang.Object)"
    //  example2 uid: "java.lang.Object"
    String endURL = uid.replaceAll("<T>", "");

    Pattern argPattern = Pattern.compile(".*\\(.*\\).*");
    if (argPattern.matcher(endURL).find()) {
      // example1
      // argumentSplit: ["java.lang.Object.equals", "java.lang.Object)"]
      // nameSplit: ["java", "lang", "Object", "equals"]
      List<String> argumentSplit = Arrays.asList(endURL.split("\\("));
      List<String> nameSplit = Arrays.asList(argumentSplit.get(0).split("\\."));

      // className: "java/lang/Object"
      // methodName: "#equals"
      // argumentsName: "#java.lang.Object-"
      String className = String.join("/", nameSplit.subList(0, nameSplit.size() - 1));
      String methodName = "#" + nameSplit.get(nameSplit.size() - 1);
      String argumentsName = argumentSplit.get(1).replaceAll("[,)]", "-");

      // endURL: "java/lang/Object.html#equals-java.lang.Object-"
      endURL = className + ".html" + methodName + "-" + argumentsName;
    } else {
      // example2
      // endURL = java/lang/Object.html
      endURL = endURL.replaceAll("\\.", "/");
      endURL = endURL + ".html";
    }
    return JAVA_BASE_URL + endURL;
  }

  void updateExternalReferences(List<MetadataFile> classMetadataFiles) {
    classMetadataFiles.forEach(
        file -> file.getReferences().forEach(ref -> updateExternalReference(ref)));
  }

  private void updateExternalReference(MetadataFileItem reference) {
    String uid = reference.getUid();
    uid = updateReferenceUid(uid);

    if (isJavaPrimitive(uid)) {
      reference.setHref(PRIMITIVE_URL);
      return;
    }
    if (isJavaLibrary(uid)) {
      reference.setHref(getJavaReferenceHref(uid));
    }
    if (isExternalReference(uid)) {
      reference.setIsExternal(true);
    }
    if (reference.getSpecForJava().size() > 0) {
      for (SpecViewModel spec : reference.getSpecForJava()) {
        String specUid = spec.getUid();
        if (specUid != null) {
          if (isJavaPrimitive(specUid)) {
            spec.setHref(PRIMITIVE_URL);
          }
          if (isJavaLibrary(specUid)) {
            spec.setHref(getJavaReferenceHref(specUid));
          }
          if (isExternalReference(specUid)) {
            spec.setIsExternal(true);
          }
        }
      }
    }
  }

  private String updateReferenceUid(String uid) {
    if (ENDING_PATTERN.matcher(uid).find()) {
      uid = uid.replace("<?>", "");
    }
    return uid;
  }

  private boolean isExternalReference(String uid) {
    return (PROTOBUF_PATTERN.matcher(uid).find()
        || GAX_PATTERN.matcher(uid).find()
        || APICOMMON_PATTERN.matcher(uid).find()
        || GAX_PATTERN.matcher(uid).find()
        || LONGRUNNING_PATTERN.matcher(uid).find());
  }

  private boolean isJavaPrimitive(String uid) {
    return (uid.equals("boolean")
        || uid.equals("int")
        || uid.equals("byte")
        || uid.equals("long")
        || uid.equals("float")
        || uid.equals("double")
        || uid.equals("char")
        || uid.equals("short"));
  }

  private boolean isJavaLibrary(String uid) {
    return JAVA_PATTERN.matcher(uid).find();
  }

  void addParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .addAll(
            methodItem.getSyntax().getParameters().stream()
                .map(parameter -> buildRefItem(parameter.getType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
  }

  void addReturnReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .addAll(
            Stream.of(methodItem.getSyntax().getReturnValue())
                .filter(Objects::nonNull)
                .map(returnValue -> buildRefItem(returnValue.getReturnType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
  }

  void addExceptionReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .addAll(
            methodItem.getExceptions().stream()
                .map(exceptionItem -> buildRefItem(exceptionItem.getType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
  }

  void addTypeParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .addAll(
            methodItem.getSyntax().getTypeParameters().stream()
                .map(
                    typeParameter -> {
                      String id = typeParameter.getId();
                      return new MetadataFileItem(id, id, false);
                    })
                .collect(Collectors.toList()));
  }

  void addSuperclassAndInterfacesReferences(
      TypeElement classElement, MetadataFile classMetadataFile) {
    classMetadataFile.getReferences().addAll(classLookup.extractReferences(classElement));
  }

  void addInnerClassesReferences(TypeElement classElement, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .addAll(
            ElementFilter.typesIn(elementUtil.extractSortedElements(classElement)).stream()
                .map(this::buildClassReference)
                .collect(Collectors.toList()));
  }

  void addOverloadReferences(MetadataFileItem item, MetadataFile classMetadataFile) {
    classMetadataFile
        .getReferences()
        .add(
            new MetadataFileItem(item.getOverload()) {
              {
                setName(RegExUtils.removeAll(item.getName(), "\\(.*\\)$"));
                setNameWithType(RegExUtils.removeAll(item.getNameWithType(), "\\(.*\\)$"));
                setFullName(RegExUtils.removeAll(item.getFullName(), "\\(.*\\)$"));
                setPackageName(item.getPackageName());
              }
            });
  }

  /**
   * Replace one record in 'references' with several records in this way:
   *
   * <pre>
   * a.b.c.List<df.mn.ClassOne<tr.T>> ->
   *     - a.b.c.List
   *     - df.mn.ClassOne
   *     - tr.T
   * </pre>
   */
  void expandComplexGenericsInReferences(MetadataFile classMetadataFile) {
    Set<MetadataFileItem> additionalItems = new LinkedHashSet<>();
    Iterator<MetadataFileItem> iterator = classMetadataFile.getReferences().iterator();
    while (iterator.hasNext()) {
      MetadataFileItem item = iterator.next();
      String uid = item.getUid();
      if (!uid.endsWith("*") && uid.contains("<")) {
        List<String> classNames = splitUidWithGenericsIntoClassNames(uid);
        additionalItems.addAll(
            classNames.stream()
                .map(s -> new MetadataFileItem(s, classLookup.makeTypeShort(s), false))
                .collect(Collectors.toSet()));
      }
    }
    // Remove items which already exist in 'items' section (compared by 'uid' field)
    additionalItems.removeAll(classMetadataFile.getItems());

    classMetadataFile.getReferences().addAll(additionalItems);
  }

  MetadataFileItem buildRefItem(String uid) {
    if (!uid.endsWith("*") && (uid.contains("<") || uid.contains("[]"))) {
      return new MetadataFileItem(uid, getJavaSpec(replaceUidAndSplit(uid)));
    } else {
      List<String> fullNameList = new ArrayList<>();

      environment
          .getIncludedElements()
          .forEach(
              element ->
                  elementUtil
                      .extractSortedElements(element)
                      .forEach(
                          typeElement ->
                              fullNameList.add(classLookup.extractFullName(typeElement))));

      if (fullNameList.contains(uid)) {
        return new MetadataFileItem(uid, classLookup.makeTypeShort(uid), false);
      } else {
        return new MetadataFileItem(uid, getJavaSpec(replaceUidAndSplit(uid)));
      }
    }
  }
}
