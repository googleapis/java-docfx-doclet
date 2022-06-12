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

import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.Status;
import com.microsoft.model.TocItem;
import com.microsoft.model.TocTypeMap;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.Utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.build.BuilderUtil.LANGS;
import static com.microsoft.build.BuilderUtil.populateItemFields;

class ClassBuilder {
    private ElementUtil elementUtil;
    private ClassLookup classLookup;
    private ClassItemsLookup classItemsLookup;
    private String outputPath;
    private ReferenceBuilder referenceBuilder;

    ClassBuilder(ElementUtil elementUtil, ClassLookup classLookup, ClassItemsLookup classItemsLookup, String outputPath, ReferenceBuilder referenceBuilder) {
        this.elementUtil = elementUtil;
        this.classLookup = classLookup;
        this.classItemsLookup = classItemsLookup;
        this.outputPath = outputPath;
        this.referenceBuilder = referenceBuilder;
    }

    void buildFilesForInnerClasses(Element element, TocTypeMap tocTypeMap, List<MetadataFile> container) {
        for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
            String uid = classLookup.extractUid(classElement);
            String name = classLookup.extractTocName(classElement);
            String status = classLookup.extractStatus(classElement);

            if (tocTypeMap.get(classElement.getKind().name()) != null) {
                if (classElement.getKind().name().equals(ElementKind.CLASS.name()) && name.contains("Exception")) {
                    tocTypeMap.get("EXCEPTION").add(new TocItem(uid, name, status));
                } else {
                    tocTypeMap.get(classElement.getKind().name()).add(new TocItem(uid, name, status));
                }
            } else {
                tocTypeMap.get(ElementKind.CLASS.name()).add(new TocItem(uid, name, status));
            }

            container.add(buildClassYmlFile(classElement));
            buildFilesForInnerClasses(classElement, tocTypeMap, container);
        }
    }

    private MetadataFile buildClassYmlFile(TypeElement classElement) {
        String fileName = classLookup.extractHref(classElement);
        MetadataFile classMetadataFile = new MetadataFile(outputPath, fileName);
        addClassInfo(classElement, classMetadataFile);
        addConstructorsInfo(classElement, classMetadataFile);
        addMethodsInfo(classElement, classMetadataFile);
        addFieldsInfo(classElement, classMetadataFile);
        referenceBuilder.addReferencesInfo(classElement, classMetadataFile);
        applyPostProcessing(classMetadataFile);
        return classMetadataFile;
    }

    private void addClassInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classItem = new MetadataFileItem(LANGS, classLookup.extractUid(classElement));
        classItem.setId(classLookup.extractId(classElement));
        classItem.setParent(classLookup.extractParent(classElement));
        addChildren(classElement, classItem.getChildren());
        populateItemFields(classItem, classLookup, classElement);
        classItem.setPackageName(classLookup.extractPackageName(classElement));
        classItem.setTypeParameters(classLookup.extractTypeParameters(classElement));
        classItem.setInheritance(classLookup.extractSuperclass(classElement));
        classItem.setInterfaces(classLookup.extractInterfaces(classElement));
        classItem.setInheritedMethods(classLookup.extractInheritedMethods(classElement));
        classItem.setSummary(classLookup.extractSummary(classElement));
        classItem.setStatus(classLookup.extractStatus(classElement));
        classMetadataFile.getItems().add(classItem);
    }

    void addConstructorsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(ElementUtil.getEnclosedElements(classElement))) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(classItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(classItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(classItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);

            referenceBuilder.addParameterReferences(constructorItem, classMetadataFile);
            referenceBuilder.addOverloadReferences(constructorItem, classMetadataFile);
        }
    }

    private void addMethodsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.methodsIn(ElementUtil.getEnclosedElements(classElement)).stream()
                .filter(methodElement -> !Utils.isPrivateOrPackagePrivate(methodElement))
                .forEach(methodElement -> {
                    MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
                    methodItem.setOverload(classItemsLookup.extractOverload(methodElement));
                    methodItem.setContent(classItemsLookup.extractMethodContent(methodElement));
                    methodItem.setExceptions(classItemsLookup.extractExceptions(methodElement));
                    methodItem.setParameters(classItemsLookup.extractParameters(methodElement));
                    methodItem.setReturn(classItemsLookup.extractReturn(methodElement));
                    methodItem.setOverridden(classItemsLookup.extractOverridden(methodElement));

                    classMetadataFile.getItems().add(methodItem);
                    referenceBuilder.addExceptionReferences(methodItem, classMetadataFile);
                    referenceBuilder.addParameterReferences(methodItem, classMetadataFile);
                    referenceBuilder.addReturnReferences(methodItem, classMetadataFile);
                    referenceBuilder.addOverloadReferences(methodItem, classMetadataFile);
                });
    }

    private void addFieldsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.fieldsIn(ElementUtil.getEnclosedElements(classElement)).stream()
                .filter(fieldElement -> !Utils.isPrivateOrPackagePrivate(fieldElement))
                .forEach(fieldElement -> {
                    MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
                    fieldItem.setContent(classItemsLookup.extractFieldContent(fieldElement));
                    fieldItem.setReturn(classItemsLookup.extractReturn(fieldElement));
                    classMetadataFile.getItems().add(fieldItem);
                    referenceBuilder.addReturnReferences(fieldItem, classMetadataFile);
                });
    }

    private void applyPostProcessing(MetadataFile classMetadataFile) {
        referenceBuilder.expandComplexGenericsInReferences(classMetadataFile);
    }

    private MetadataFileItem buildMetadataFileItem(Element element) {
        return new MetadataFileItem(LANGS, classItemsLookup.extractUid(element)) {{
            String name = classItemsLookup.extractName(element);
            setId(classItemsLookup.extractId(element));
            setParent(classItemsLookup.extractParent(element));
            setName(name);
            setNameWithType(classItemsLookup.extractNameWithType(element));
            setFullName(classItemsLookup.extractFullName(element));
            setType(classItemsLookup.extractType(element));
            setJavaType(classItemsLookup.extractJavaType(element));
            setPackageName(classItemsLookup.extractPackageName(element));
            setSummary(classItemsLookup.extractSummary(element));
            setStatus(classItemsLookup.extractStatus(element));
        }};
    }

    private void addChildren(TypeElement classElement, List<String> children) {
        collect(classElement, children, ElementFilter::constructorsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::methodsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::fieldsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::typesIn, String::valueOf);
    }

    private void collect(TypeElement classElement, List<String> children,
                 Function<Iterable<? extends Element>, List<? extends Element>> selectFunc,
                 Function<? super Element, String> mapFunc) {

        List<? extends Element> elements = selectFunc.apply(ElementUtil.getEnclosedElements(classElement));
        children.addAll(filterPrivateElements(elements).stream()
                .map(mapFunc).collect(Collectors.toList()));
    }

    private List<? extends Element> filterPrivateElements(List<? extends Element> elements) {
        return elements.stream()
                .filter(element -> !Utils.isPrivateOrPackagePrivate(element)).collect(Collectors.toList());
    }
}
