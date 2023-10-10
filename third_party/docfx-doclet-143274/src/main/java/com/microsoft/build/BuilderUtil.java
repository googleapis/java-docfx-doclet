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

import com.microsoft.lookup.BaseLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.SpecViewModel;
import com.microsoft.util.YamlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

final class BuilderUtil {
  private static final Pattern XREF_LINK_PATTERN =
      Pattern.compile("<xref uid=\".*?\" .*?>.*?</xref>");
  private static final Pattern XREF_LINK_CONTENT_PATTERN =
      Pattern.compile("(?<=<xref uid=\").*?(?=\" .*?>.*?</xref>)");
  private static final Pattern XREF_LINK_RESOLVE_PATTERN =
      Pattern.compile("(?<class>\\w+)\\#(?<member>\\w+)(\\((?<param>.*)\\))?");
  public static final String[] LANGS = {"java"};

  static String populateUidValues(String text, LookupContext lookupContext) {
    if (StringUtils.isBlank(text)) {
      return text;
    }

    Matcher linkMatcher = XREF_LINK_PATTERN.matcher(text);
    while (linkMatcher.find()) {
      String link = linkMatcher.group();
      Matcher linkContentMatcher = XREF_LINK_CONTENT_PATTERN.matcher(link);
      if (!linkContentMatcher.find()) {
        continue;
      }

      String linkContent = linkContentMatcher.group();
      String uid = resolveUidFromLinkContent(linkContent, lookupContext);
      String updatedLink = linkContentMatcher.replaceAll(uid);
      text = StringUtils.replace(text, link, updatedLink);
    }
    return text;
  }

  /**
   * The linkContent could be in following format #memeber Class#member Class#method()
   * Class#method(params)
   */
  static String resolveUidFromLinkContent(String linkContent, LookupContext lookupContext) {
    if (StringUtils.isBlank(linkContent)) {
      return "";
    }

    linkContent = linkContent.trim();

    // complete class name for class internal link
    if (linkContent.startsWith("#")) {
      String firstKey = lookupContext.getOwnerUid();
      linkContent = firstKey + linkContent;
    }

    // fuzzy resolve, target for items from project external references
    String fuzzyResolvedUid = resolveUidFromReference(linkContent, lookupContext);

    // exact resolve in lookupContext
    linkContent = linkContent.replace("#", ".");
    String exactResolveUid = resolveUidByLookup(linkContent, lookupContext);
    return exactResolveUid.isEmpty() ? fuzzyResolvedUid : exactResolveUid;
  }

  static List<String> splitUidWithGenericsIntoClassNames(String uid) {
    uid = RegExUtils.removeAll(uid, "[>]+$");
    return Arrays.asList(StringUtils.split(uid, "<"));
  }

  static List<String> replaceUidAndSplit(String uid) {
    String retValue = RegExUtils.replaceAll(uid, "\\<", "//<//");
    retValue = RegExUtils.replaceAll(retValue, "\\>", "//>//");
    retValue = RegExUtils.replaceAll(retValue, ",", "//,//");
    retValue = RegExUtils.replaceAll(retValue, "\\[\\]", "//[]//");

    return Arrays.asList(StringUtils.split(retValue, "//"));
  }

  static List<SpecViewModel> getJavaSpec(List<String> references) {
    List<SpecViewModel> specList = new ArrayList<>();

    Optional.ofNullable(references)
        .ifPresent(
            ref ->
                references.forEach(
                    uid -> {
                      if (uid.equalsIgnoreCase("<")
                          || uid.equalsIgnoreCase(">")
                          || uid.equalsIgnoreCase(",")
                          || uid.equalsIgnoreCase("[]")) specList.add(new SpecViewModel(null, uid));
                      else if (uid != "") specList.add(new SpecViewModel(uid, uid));
                    }));

    return specList;
  }

  static void populateUidValues(
      List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
    Lookup lookup = new Lookup(packageMetadataFiles, classMetadataFiles);

    classMetadataFiles.forEach(
        classMetadataFile -> {
          LookupContext lookupContext = lookup.buildContext(classMetadataFile);

          for (MetadataFileItem item : classMetadataFile.getItems()) {
            item.setSummary(
                YamlUtil.cleanupHtml(populateUidValues(item.getSummary(), lookupContext)));

            Optional.ofNullable(item.getSyntax())
                .ifPresent(
                    syntax -> {
                      Optional.ofNullable(syntax.getParameters())
                          .ifPresent(
                              methodParams ->
                                  methodParams.forEach(
                                      param -> {
                                        param.setDescription(
                                            populateUidValues(
                                                param.getDescription(), lookupContext));
                                      }));
                      Optional.ofNullable(syntax.getReturnValue())
                          .ifPresent(
                              returnValue ->
                                  returnValue.setReturnDescription(
                                      populateUidValues(
                                          syntax.getReturnValue().getReturnDescription(),
                                          lookupContext)));
                    });
          }
        });
  }

  /**
   * this method is used to do fuzzy resolve "*" will be added at the end of uid for method for xerf
   * service resolve purpose
   */
  static String resolveUidFromReference(String linkContent, LookupContext lookupContext) {
    String uid = "";
    Matcher matcher = XREF_LINK_RESOLVE_PATTERN.matcher(linkContent);

    if (matcher.find()) {
      String className = matcher.group("class");
      String memberName = matcher.group("member");
      uid = resolveUidByLookup(className, lookupContext);
      if (!uid.isEmpty()) {
        uid = uid.concat(".").concat(memberName);

        // linkContent targets a method
        if (!StringUtils.isBlank(matcher.group(3))) {
          uid = uid.concat("*");
        }
      }
    }
    return uid;
  }

  static String resolveUidByLookup(String signature, LookupContext lookupContext) {
    if (StringUtils.isBlank(signature) || lookupContext == null) {
      return "";
    }
    return lookupContext.containsKey(signature) ? lookupContext.resolve(signature) : "";
  }

  static <T extends Element> void populateItemFields(
      MetadataFileItem item, BaseLookup<T> lookup, T element) {
    String name = lookup.extractName(element);
    item.setName(name);
    item.setNameWithType(lookup.extractNameWithType(element));
    item.setFullName(lookup.extractFullName(element));
    item.setType(lookup.extractType(element));
    item.setJavaType(lookup.extractJavaType(element));
    item.setSummary(lookup.extractSummary(element));
    item.setStatus(lookup.extractStatus(element));
    item.setContent(lookup.extractContent(element));
  }

  /** Does not include syntax contents for Client classes as they are not useful */
  static <T extends Element> void populateItemFieldsForClients(
      MetadataFileItem item, BaseLookup<T> lookup, T element) {
    String name = lookup.extractName(element);
    item.setName(name);
    item.setNameWithType(lookup.extractNameWithType(element));
    item.setFullName(lookup.extractFullName(element));
    item.setType(lookup.extractType(element));
    item.setJavaType(lookup.extractJavaType(element));
    item.setSummary(lookup.extractSummary(element));
    item.setStatus(lookup.extractStatus(element));
  }
}
