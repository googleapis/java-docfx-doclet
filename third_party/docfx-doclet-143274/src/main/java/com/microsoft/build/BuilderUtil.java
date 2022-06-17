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
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BuilderUtil {
    private static final Pattern XREF_LINK_PATTERN = Pattern.compile("<xref uid=\".*?\" .*?>.*?</xref>");
    private static final Pattern XREF_LINK_CONTENT_PATTERN = Pattern.compile("(?<=<xref uid=\").*?(?=\" .*?>.*?</xref>)");
    private static final Pattern XREF_LINK_RESOLVE_PATTERN = Pattern.compile("(?<class>\\w+)\\#(?<member>\\w+)(\\((?<param>.*)\\))?");
    public final static String[] LANGS = {"java"};

    /**
     * Replaces all the references the link in the linkContent with the UID
     *
     * @param text Text that potentially contains a link reference
     * @param packageName MetaFileItem's packageName
     * @param lookupContext Lookup Context
     * @return Text with a link reference to the full qualified link
     */
    static String populateUidValues(String text, String packageName, LookupContext lookupContext) {
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
            String uid = resolveUidFromLinkContent(linkContent, packageName, lookupContext);
            String updatedLink = linkContentMatcher.replaceAll(uid);
            text = StringUtils.replace(text, link, updatedLink);
        }
        return text;
    }

    /**
     *
     * The linkContent could be in following format
     * 1. #member
     * 2. Class#member
     * 3. Class#method()
     * 4. Class#method(params)
     * 5. Package.Class# +{Any combination of above}
     * All Possibilities listed: https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#link
     *
     * @param linkContent Text of the link
     * @param packageName MetaFileItem's packageName
     * @param lookupContext LookupContext
     * @return Fully qualified link url
     */
    static String resolveUidFromLinkContent(String linkContent, String packageName, LookupContext lookupContext) {
        if (StringUtils.isBlank(linkContent)) {
            return "";
        }

        linkContent = linkContent.trim();

        // complete class name for class internal link
        if (linkContent.startsWith("#")) {
            // Can't use packageName because it is missing the ClassName
            String firstKey = lookupContext.getOwnerUid();
            linkContent = firstKey + linkContent;
        }
        // fuzzy resolve, target for items from project external references
        String fuzzyResolvedUid = resolveUidFromReference(linkContent, lookupContext);

        // exact resolve in lookupContext
        linkContent = linkContent.replace("#", ".");
        String qualifiedLink = getFullyQualifiedLinkUrl(linkContent, packageName);

        // First, prefer references inside local package
        String exactResolveUid = resolveUidByLookup(qualifiedLink, lookupContext);
        if (exactResolveUid.isEmpty()) {
            // Resolve with original linkContent
            exactResolveUid = resolveUidByLookup(linkContent, lookupContext);
        }
        // Resolve with fuzzyResolve / external references
        return exactResolveUid.isEmpty() ? fuzzyResolvedUid : exactResolveUid;
    }

    /**
     * Logic to ensure that resulting link is in the format Package.Class.Method(Params...)
     *
     * @param linkContent String of the Class#Method
     * @param packageName Package Name that should go in front of the link
     * @return Fully qualified link url
     */
    private static String getFullyQualifiedLinkUrl(String linkContent, String packageName) {
        // If packageName does not exist/error'd or is already at the beginning of the link
        if (packageName == null || linkContent.indexOf(packageName) == 0) {
            return linkContent;
        }
        return String.format("%s.%s", packageName, linkContent);
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

        Optional.ofNullable(references).ifPresent(
                ref -> references.forEach(
                        uid -> {
                            if (uid.equalsIgnoreCase("<")
                                    || uid.equalsIgnoreCase(">")
                                    || uid.equalsIgnoreCase(",")
                                    || uid.equalsIgnoreCase("[]"))
                                specList.add(new SpecViewModel(null, uid));
                            else if (uid != "")
                                specList.add(new SpecViewModel(uid, uid));
                        })
        );

        return specList;
    }

    /**
     * Populate the links for references based on a UID. Looker generates mappings for local context
     * (file specific) and global context (all the UID references). Searching is done first in the
     * local context and then in the global context if local context is not found.
     *
     * ex. {@link Lookup } would search all the references to find which Lookup file to use
     * It would parse the text to search ("Lookup")
     *
     * Since packages (v1 and v1beta) may contain the same generated java file names, there may be some
     * conflicts between which link it should be.
     *
     * The Looker#consumer() function guarantees that the UID (+ other combinations) will be put into the LookupContext.
     * As long as the link that we extract contains Package#Method, it will match in either local or global context
     *
     * @param packageMetadataFiles Package specific metadata files
     * @param classMetadataFiles Class specific metadata files
     */
    static void populateUidValues(List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
        Lookup lookup = new Lookup(packageMetadataFiles, classMetadataFiles);

        classMetadataFiles.forEach(classMetadataFile -> {
            LookupContext lookupContext = lookup.buildContext(classMetadataFile);

            for (MetadataFileItem item : classMetadataFile.getItems()) {
                String packageName = item.getPackageName();
                item.setSummary(YamlUtil.cleanupHtml(
                        populateUidValues(item.getSummary(), packageName, lookupContext)
                ));

                Optional.ofNullable(item.getSyntax()).ifPresent(syntax -> {
                            Optional.ofNullable(syntax.getParameters()).ifPresent(
                                    methodParams -> methodParams.forEach(
                                            param -> param.setDescription(populateUidValues(param.getDescription(), packageName, lookupContext)))
                            );
                            Optional.ofNullable(syntax.getReturnValue()).ifPresent(returnValue ->
                                    returnValue.setReturnDescription(
                                            populateUidValues(syntax.getReturnValue().getReturnDescription(), packageName, lookupContext)
                                    )
                            );
                        }
                );
            }
        });
    }

    /**
     * this method is used to do fuzzy resolve
     * "*" will be added at the end of uid for method for xerf service resolve purpose
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

    static <T extends Element> void populateItemFields(MetadataFileItem item, BaseLookup<T> lookup, T element) {
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
}
