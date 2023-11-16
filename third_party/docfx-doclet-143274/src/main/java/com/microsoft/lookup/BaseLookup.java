package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.YamlUtil;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.SeeTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public abstract class BaseLookup<T extends Element> {

  private static final int INITIAL_CAPACITY = 500000;
  protected final Map<ElementKind, String> elementKindLookup =
      new HashMap<>() {
        {
          put(ElementKind.PACKAGE, "Namespace");
          put(ElementKind.CLASS, "Class");
          put(ElementKind.ENUM, "Enum");
          put(ElementKind.ENUM_CONSTANT, "Field");
          put(ElementKind.INTERFACE, "Interface");
          put(ElementKind.ANNOTATION_TYPE, "Interface");
          put(ElementKind.CONSTRUCTOR, "Constructor");
          put(ElementKind.METHOD, "Method");
          put(ElementKind.FIELD, "Field");
        }
      };

  protected Map<T, ExtendedMetadataFileItem> map;
  protected final DocletEnvironment environment;

  protected BaseLookup(DocletEnvironment environment) {
    this.environment = environment;
    this.map = new HashMap<>(INITIAL_CAPACITY);
  }

  protected ExtendedMetadataFileItem resolve(T key) {
    map.computeIfAbsent(key, this::buildMetadataFileItem);
    return map.get(key);
  }

  protected abstract ExtendedMetadataFileItem buildMetadataFileItem(T key);

  public String extractPackageName(T key) {
    return resolve(key).getPackageName();
  }

  public String extractFullName(T key) {
    return resolve(key).getFullName();
  }

  public String extractName(T key) {
    return resolve(key).getName();
  }

  public String extractHref(T key) {
    return resolve(key).getHref();
  }

  public String extractParent(T key) {
    return resolve(key).getParent();
  }

  public String extractId(T key) {
    return resolve(key).getId();
  }

  public String extractUid(T key) {
    return resolve(key).getUid();
  }

  public String extractNameWithType(T key) {
    return resolve(key).getNameWithType();
  }

  public String extractMethodContent(T key) {
    return resolve(key).getMethodContent();
  }

  public String extractFieldContent(T key) {
    return resolve(key).getFieldContent();
  }

  public String extractConstructorContent(T key) {
    return resolve(key).getConstructorContent();
  }

  public String extractOverload(T key) {
    return resolve(key).getOverload();
  }

  public List<MethodParameter> extractParameters(T key) {
    return resolve(key).getParameters();
  }

  public List<ExceptionItem> extractExceptions(T key) {
    return resolve(key).getExceptions();
  }

  public Return extractReturn(T key) {
    return resolve(key).getReturn();
  }

  public String extractSummary(T key) {
    return resolve(key).getSummary();
  }

  public String extractType(T key) {
    return resolve(key).getType();
  }

  public String extractJavaType(T element) {
    return null;
  }

  public String extractContent(T key) {
    return resolve(key).getContent();
  }

  public List<TypeParameter> extractTypeParameters(T key) {
    return resolve(key).getTypeParameters();
  }

  public List<String> extractSuperclass(T key) {
    List<String> reversed = resolve(key).getSuperclass();
    Collections.reverse(reversed);
    return reversed;
  }

  public List<String> extractInheritedMethods(T key) {
    List<String> sorted = resolve(key).getInheritedMethods();
    Collections.sort(sorted);
    return sorted;
  }

  public List<String> extractInterfaces(T key) {
    return resolve(key).getInterfaces();
  }

  public String extractTocName(T key) {
    return resolve(key).getTocName();
  }

  public Set<MetadataFileItem> extractReferences(T key) {
    return resolve(key).getReferences();
  }

  public String extractOverridden(T key) {
    return resolve(key).getOverridden();
  }

  protected Optional<DocCommentTree> getDocCommentTree(T element) {
    return Optional.ofNullable(environment.getDocTrees().getDocCommentTree(element));
  }

  private boolean hasDeprecatedJavadocTag(T element) {
    List<? extends DocTree> javadocTags =
        getDocCommentTree(element)
            .map(DocCommentTree::getBlockTags)
            .orElse(Collections.emptyList());

    return javadocTags.stream().map(DocTree::getKind).anyMatch(DocTree.Kind.DEPRECATED::equals);
  }

  protected String determineType(T element) {
    return elementKindLookup.get(element.getKind());
  }

  protected String determinePackageName(T element) {
    return String.valueOf(environment.getElementUtils().getPackageOf(element));
  }

  protected String determineComment(T element) {
    String statusComment = getStatusComment(element);
    String javadocComment = getJavadocComment(element);
    return joinNullable(statusComment, javadocComment);
  }

  private String getJavadocComment(T element) {
    return getDocCommentTree(element)
        .map(
            tree -> {
              String commentWithBlockTags = replaceLinksAndCodes(tree.getFullBody());
              return replaceBlockTags(tree, commentWithBlockTags);
            })
        .orElse(null);
  }

  /**
   * Safely combine two nullable strings with a newline delimiter
   */
  String joinNullable(@Nullable String top, @Nullable String bottom) {
    String a = top == null || top.isEmpty() ? null : top;
    String b = bottom == null || bottom.isEmpty() ? null : bottom;

    if (a != null && b != null) {
      return a + "\n\n" + b;
    } else if (a != null) {
      return a;
    } else {
      return b;
    }
  }

  /**
   * Provides support for deprecated and see tags
   */
  String replaceBlockTags(DocCommentTree docCommentTree, String comment) {
    Set<String> seeItems = new HashSet<>();
    String commentWithBlockTags = comment;
    for (DocTree blockTag : docCommentTree.getBlockTags()) {
      switch (blockTag.getKind()) {
        case DEPRECATED:
          commentWithBlockTags = getDeprecatedSummary((DeprecatedTree) blockTag).concat(comment);
          break;
        case SEE:
          seeItems.add(getSeeTagRef((SeeTree) blockTag));
          break;
        default:
      }
    }
    if (!seeItems.isEmpty()) {
      commentWithBlockTags = commentWithBlockTags.concat(getSeeAlsoSummary(seeItems));
    }
    return commentWithBlockTags;
  }

  /**
   * <ul>
   * <li>Replace @link and @linkplain with <xref> tags</li>
   * <li>Replace @code with <code> tags</li>
   * </ul>
   */
  String replaceLinksAndCodes(List<? extends DocTree> items) {
    return YamlUtil.cleanupHtml(
        items.stream()
            .map(
                bodyItem -> {
                  switch (bodyItem.getKind()) {
                    case LINK:
                    case LINK_PLAIN:
                      return buildXrefTag((LinkTree) bodyItem);
                    case CODE:
                      return buildCodeTag((LiteralTree) bodyItem);
                    case LITERAL:
                      return expandLiteralBody((LiteralTree) bodyItem);
                    default:
                      return String.valueOf(StringEscapeUtils.unescapeJava(bodyItem.toString()));
                  }
                })
            .collect(Collectors.joining()));
  }

  /**
   * By using this way of processing links we provide support of @links with label, like this:
   * {@link List someLabel}
   */
  String buildXrefTag(LinkTree linkTree) {
    String signature = linkTree.getReference().getSignature();
    String label =
        linkTree.getLabel().stream().map(String::valueOf).collect(Collectors.joining(" "));
    if (StringUtils.isEmpty(label)) {
      label = signature;
    }
    return String.format(
        "<xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref>", signature, label);
  }

  String buildCodeTag(LiteralTree literalTree) {
    return String.format(
        "<code>%s</code>", StringEscapeUtils.unescapeJava(literalTree.getBody().toString()));
  }

  String expandLiteralBody(LiteralTree bodyItem) {
    return String.valueOf(StringEscapeUtils.unescapeJava(bodyItem.getBody().toString()));
  }

  /**
   * We make type shortening in assumption that package name doesn't contain uppercase characters
   */
  public String makeTypeShort(String value) {
    if (!value.contains(".")) {
      return value;
    }
    return Stream.of(StringUtils.split(value, "<"))
        .map(s -> RegExUtils.removeAll(s, "\\b[a-z0-9_.]+\\."))
        .collect(Collectors.joining("<"));
  }

  private String getSeeAlsoSummary(Set<String> seeItems) {
    return String.format("\nSee Also: %s\n", String.join(", ", seeItems));
  }

  private String getDeprecatedSummary(DeprecatedTree deprecatedTree) {
    return String.format(
        "\n<strong>Deprecated.</strong> <em>%s</em>\n\n",
        replaceLinksAndCodes(deprecatedTree.getBody()));
  }

  private String getSeeTagRef(SeeTree seeTree) {
    String ref =
        seeTree.getReference().stream().map(r -> String.valueOf(r)).collect(Collectors.joining(""));
    // if it's already a tag, use that otherwise build xref tag
    if (ref.matches("^<.+>(.|\n)*")) {
      return ref.replaceAll("\n", "").replaceAll("(  )+", " ");
    }
    return String.format(
        "<xref uid=\"%1$s\" data-throw-if-not-resolved=\"false\">%1$s</xref>", ref);
  }

  public String extractStatus(T element) {
    List<String> annotationNames =
        element.getAnnotationMirrors().stream()
            .map(mirror -> mirror.getAnnotationType().asElement().getSimpleName().toString())
            .collect(Collectors.toList());

    if (annotationNames.stream().anyMatch("InternalApi"::equals)
        || annotationNames.stream().anyMatch("InternalExtensionOnly"::equals)) {
      return "internal";
    }
    if (annotationNames.stream().anyMatch("Deprecated"::equals)
        || hasDeprecatedJavadocTag(element)) {
      return "deprecated";
    }
    if (annotationNames.stream().anyMatch("ObsoleteApi"::equals)) {
      return "obsolete";
    }
    if (annotationNames.stream().anyMatch("BetaApi"::equals)) {
      return "beta";
    }
    return null;
  }

  public String getStatusComment(T element) {
    Map<String, Optional<String>> annotationComments = getAnnotationComments(element);

    // Deprecated comments are determined by the Javadoc @deprecated block tag.
    // See this#replaceBlockTags

    List<String> comments = new ArrayList<>();
    if (annotationComments.containsKey("InternalApi")) {
      comments.add(createInternalOnlyNotice(annotationComments.get("InternalApi")));
    }
    if (annotationComments.containsKey("InternalExtensionOnly")) {
      comments.add(createInternalExtensionOnlyNotice(annotationComments.get("InternalExtensionOnly")));
    }
    if (annotationComments.containsKey("ObsoleteApi")) {
      comments.add(createObsoleteNotice(annotationComments.get("ObsoleteApi")));
    }
    if (annotationComments.containsKey("BetaApi")) {
      comments.add(createBetaNotice(annotationComments.get("BetaApi")));
    }

    if (comments.isEmpty()) {
      return null;
    }
    return String.join("\n\n", comments);
  }
  private String createBetaNotice(Optional<String> customComment) {
    return "<aside class=\"beta\">\n"
        + "<p><strong>Beta</strong></p>\n"
        + customComment.map(comment -> "<p><em>" + comment + "</em></p>\n").orElse("")
        + "<p>This feature is covered by the <a href=\"/terms/service-terms#1\">Pre-GA Offerings "
        + "Terms</a> of the Terms of Service. Pre-GA libraries might have limited support, and "
        + "changes to pre-GA libraries might not be compatible with other pre-GA versions. For "
        + "more information, see the launch stage descriptions.</p>\n"
        + "</aside>\n";
  }
  private String createObsoleteNotice(Optional<String> customComment) {
    return "<aside class=\"deprecated\">\n"
        + "<p><strong>Obsolete</strong></p>\n"
        + customComment.map(comment -> "<p><em>" + comment + "</em></p>\n").orElse("")
        + "<p>This feature is stable for usage in this major version, but may be deprecated in a "
        + "future release.</p>\n"
        + "</aside>\n";
  }
  private String createInternalExtensionOnlyNotice(Optional<String> customComment) {
    return "<aside class=\"special\">\n"
        + "<p><strong>Internal Extension Only</strong>: This feature is stable for usage, but is "
        + "not intended for extension or implementation.</p>\n"
        + customComment.map(comment -> "<p><em>" + comment + "</em></p>\n").orElse("")
        + "</aside>\n";
  }
  private String createInternalOnlyNotice(Optional<String> customComment) {
    return "<aside class=\"warning\">\n"
        + "<p><strong>Internal Only</strong>: This feature is not stable for application use.</p>\n"
        + customComment.map(comment -> "<p><em>" + comment + "</em></p>\n").orElse("")
        + "</aside>\n";
  }

  /**
   * @return all annotations on the element and their associated comment, if it exists
   */
  public Map<String, Optional<String>> getAnnotationComments(T element) {
    Map<String, Optional<String>> annotationComments = new HashMap<>();

    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      String name = annotation.getAnnotationType().asElement().getSimpleName().toString();
      Optional<String> value =
          annotation.getElementValues().entrySet().stream()
              .filter(entry -> entry.getKey().getSimpleName().toString().equals("value"))
              .map(Map.Entry::getValue)
              .map(annotationValue -> annotationValue.getValue().toString())
              .findFirst();

      annotationComments.put(name, value);
    }

    return annotationComments;
  }
}
