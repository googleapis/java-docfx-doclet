package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.LANGS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.docfx.doclet.RepoMetadata;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFileItem;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.PackageElement;

public class PackageOverviewFile {
  private String CLIENT_TABLE_HEADER = "";
  private String SETTINGS_TABLE_HEADER = "";
  private String CLASSES_TABLE_HEADER = "";
  private String INTERFACES_TABLE_HEADER = "";

  private String STUB_TABLE_HEADER = "";
  private String CALLABLE_FACTORY_TABLE_HEADER = "";

  private String ENUM_TABLE_HEADER = "";
  private String EXCEPTION_TABLE_HEADER = "";

  private String CLIENT_TABLE_BLURB = "";

  private String SETTINGS_TABLE_BLURB = "";
  private String CLASSES_TABLE_BLURB = "";
  private String INTERFACES_TABLE_BLURB = "";

  private String STUB_TABLE_BLURB = "";
  private String CALLABLE_FACTORY_TABLE_BLURB = "";

  private String ENUM_TABLE_BLURB = "";
  private String EXCEPTION_TABLE_BLURB = "";
  private String CLIENT_TABLE = "";

  private String SETTINGS_TABLE = "";

  private String CLASSES_TABLE = "";

  private String STUB_TABLE = "";
  private String CALLABLE_FACTORY_TABLE = "";

  private String INTERFACES_TABLE = "";

  private String ENUM_TABLE = "";

  private String EXCEPTION_TABLE = "";

  // This is only set if the package is not a GA package
  private String PRERELEASE_IMPLICATIONS = "";

  // Uses the `recommended_package` field set in the RepoMetadata file if set; otherwise computes
  // it.
  private String recommendedPackage;

  private String recommendedPackageLink;

  // This is only set if the package is not the recommended package
  private String RECOMMENDED_VERSION = "";

  // This is only set if the package is a stub package
  private String STUB_IMPLICATIONS = "";

  private String GITHUB_SOURCE_TABLE;

  private String PACKAGE_HEADER;

  private final String outputPath;
  private String fileName;

  private final PackageElement packageElement;

  public PackageOverviewFile(
      String outputPath,
      String fileName,
      RepoMetadata repoMetadata,
      PackageElement packageElement,
      String status,
      PackageLookup packageLookup,
      ReferenceBuilder referenceBuilder,
      String artifactVersion,
      String recommendedPackage) {
    this.outputPath = outputPath;
    this.fileName = fileName;
    this.packageElement = packageElement;
    this.recommendedPackage = recommendedPackage;

    String packageURIPath = fileName.replace(".md", "");

    this.PACKAGE_HEADER = "# Package " + packageURIPath + " (" + artifactVersion + ")\n";

    String cloudRADChildElementLinkPrefix =
        "https://cloud.google.com/java/docs/reference/" + repoMetadata.getArtifactId() + "/latest/";

    String packageURIPathGithub = packageURIPath.replace('.', '/');
    String githubSourcePackageLink =
        repoMetadata.getGithubLink()
            + "/"
            + repoMetadata.getArtifactId()
            + "/src/main/java/"
            + packageURIPathGithub;

    this.recommendedPackageLink = cloudRADChildElementLinkPrefix + this.recommendedPackage;
    // If the package status is not a GA version, then add a disclaimer around prerelease
    // implications
    if (status != null) {
      this.PRERELEASE_IMPLICATIONS =
          "## Prerelease Implications\n\n"
              + "This package is a prerelease version! Use with caution.\n\n"
              + "Prerelease versions are considered unstable as they may be shut down and/or subject to breaking changes when upgrading.\n"
              + "Use them only for testing or if you specifically need their experimental features.\n\n";
    }

    // If a package is not the same as the recommended package, add a disclaimer. If the recommended
    // package does not exist, then do not set the disclaimer.
    if (!this.recommendedPackage.isEmpty()
        && !packageElement.getQualifiedName().toString().equals(this.recommendedPackage)) {
      this.RECOMMENDED_VERSION =
          "## This package is not the recommended entry point to using this client library!\n\n"
              + " For this library, we recommend using ["
              + recommendedPackage
              + "]("
              + recommendedPackageLink
              + ")"
              + " for new applications.\n"
              + "\n";
    }

    // Link to recommended package (if it exists) for the Stub class as well
    if (!this.recommendedPackage.isEmpty()
        && String.valueOf(this.packageElement.getQualifiedName()).contains("stub")) {
      this.STUB_IMPLICATIONS =
          "## Stub Package Implications\n\n"
              + "This package is a a base stub class. It is for advanced usage and reflects the underlying API directly.\n"
              + "We generally recommend using the non-stub, latest GA package, such as ["
              + recommendedPackage
              + "]("
              + recommendedPackageLink
              + ")"
              + ". Use with caution.\n";
    } else if (String.valueOf(this.packageElement.getQualifiedName()).contains("stub")) {
      this.STUB_IMPLICATIONS =
          "## Stub Package Implications\n\n"
              + "This package is a a base stub class. It is for advanced usage and reflects the underlying API directly.\n"
              + "We generally recommend using non-stub classes. Use with caution.\n";
    }

    StringBuilder githubSourceTableBuilder = new StringBuilder();

    // Start of the reference link table
    githubSourceTableBuilder
        .append("<table>\n")
        .append("   <tr>\n")
        .append("     <td><a href=\"")
        .append(githubSourcePackageLink)
        .append("\">GitHub Repository</a></td>\n");

    // If RPC documentation URI exists, add to the package overview table
    if (repoMetadata.getRpcDocumentationUri().isPresent()) {
      githubSourceTableBuilder
          .append("     <td><a href=\"")
          .append(repoMetadata.getRpcDocumentationUri().get())
          .append("\">RPC Documentation</a></td>\n");
    }
    // If REST documentation URI exists, add to the package overview table
    if (repoMetadata.getRestDocumentationUri().isPresent()) {
      githubSourceTableBuilder
          .append("     <td><a href=\"")
          .append(repoMetadata.getRestDocumentationUri().get())
          .append("\">REST Documentation</a></td>\n");
    }
    githubSourceTableBuilder.append("   </tr>\n").append(" </table>").append("\n\n");

    this.GITHUB_SOURCE_TABLE = githubSourceTableBuilder.toString();

    // Prepare to build tables of different types of package children elements
    MetadataFileItem packageItem =
        new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
    referenceBuilder.addPackageChildrenSummaries(
        packageElement, packageItem.getPackageChildrenSummaries());
    List<PackageChildSummary> listOfPackageChildrenSummaries =
        packageItem.getPackageChildrenSummaries();
    listOfPackageChildrenSummaries.sort(
        Comparator.comparing(PackageChildSummary::getType)
            .thenComparing(PackageChildSummary::getUid));

    // If Clients exist in this package, create a table of them
    boolean containsClientSettingsClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Client".equals(packageChildSummary.getType()));
    if (containsClientSettingsClasses) {
      this.CLIENT_TABLE_HEADER = "## Client Classes\n";
      this.CLIENT_TABLE_BLURB =
          "Client classes are the main entry point to using a package.\nThey contain several variations of Java methods for each of the API's methods.\n";
      this.CLIENT_TABLE =
          createHtmlTable("Client", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If Settings exist in this package, create a table of them
    boolean containsSettingsClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Settings".equals(packageChildSummary.getType()));
    if (containsSettingsClasses) {
      this.SETTINGS_TABLE_HEADER = "## Settings Classes\n";
      if (packageLookup.isApiVersionStubPackage(this.packageElement)) {
        this.SETTINGS_TABLE_BLURB =
            "Settings classes can be used to configure credentials, endpoints, and retry settings for a Stub.\n";
      } else {
        this.SETTINGS_TABLE_BLURB =
            "Settings classes can be used to configure credentials, endpoints, and retry settings for a Client.\n";
      }

      this.SETTINGS_TABLE =
          createHtmlTable(
              "Settings", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If package is a Stub package, create a table of Stub classes
    boolean containsStubClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Stub".equals(packageChildSummary.getType()));
    if (containsStubClasses && (packageLookup.isApiVersionStubPackage(this.packageElement))) {
      this.STUB_TABLE_HEADER = "## Stub Classes\n";
      this.STUB_TABLE_BLURB = "";
      this.STUB_TABLE =
          createHtmlTable("Stub", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If package is a Stub package and Callable Factory classes exist in this package, create a
    // table of them
    boolean containsCallableFactoryClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(
                packageChildSummary -> "CallableFactory".equals(packageChildSummary.getType()));
    if (containsCallableFactoryClasses
        && (packageLookup.isApiVersionStubPackage(this.packageElement))) {
      this.CALLABLE_FACTORY_TABLE_HEADER = "## Callable Factory Classes\n";
      this.CALLABLE_FACTORY_TABLE_BLURB = "";
      this.CALLABLE_FACTORY_TABLE =
          createHtmlTable(
              "CallableFactory", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If Classes exist in this package, create a table of them
    boolean containsClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Class".equals(packageChildSummary.getType()));
    if (containsClasses) {
      this.CLASSES_TABLE_HEADER = "## Classes\n";
      this.CLASSES_TABLE =
          createHtmlTable("Class", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    //  If Interfaces exist in this package, create a table of them
    boolean containsInterfaces =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Interface".equals(packageChildSummary.getType()));
    if (containsInterfaces) {
      this.INTERFACES_TABLE_HEADER = "## Interfaces\n";
      this.INTERFACES_TABLE =
          createHtmlTable(
              "Interface", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If Enums exist in this package, create a table of them
    boolean containsEnums =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Enum".equals(packageChildSummary.getType()));
    if (containsEnums) {
      this.ENUM_TABLE_HEADER = "## Enums\n";
      this.ENUM_TABLE =
          createHtmlTable("Enum", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    //  If Exceptions exist in this package, create a table of them
    boolean containsExceptions =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Exception".equals(packageChildSummary.getType()));
    if (containsExceptions) {
      this.EXCEPTION_TABLE_HEADER = "## Exceptions\n";
      this.EXCEPTION_TABLE =
          createHtmlTable(
              "Exception", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }
  }

  @JsonIgnore
  public String getFileContent() {
    return PACKAGE_HEADER
        + GITHUB_SOURCE_TABLE
        + RECOMMENDED_VERSION
        + PRERELEASE_IMPLICATIONS
        + STUB_IMPLICATIONS
        + CLIENT_TABLE_HEADER
        + CLIENT_TABLE_BLURB
        + CLIENT_TABLE
        + STUB_TABLE_HEADER
        + STUB_TABLE_BLURB
        + STUB_TABLE
        + SETTINGS_TABLE_HEADER
        + SETTINGS_TABLE_BLURB
        + SETTINGS_TABLE
        + CALLABLE_FACTORY_TABLE_HEADER
        + CALLABLE_FACTORY_TABLE_BLURB
        + CALLABLE_FACTORY_TABLE
        + CLASSES_TABLE_HEADER
        + CLASSES_TABLE_BLURB
        + CLASSES_TABLE
        + INTERFACES_TABLE_HEADER
        + INTERFACES_TABLE_BLURB
        + INTERFACES_TABLE
        + ENUM_TABLE_HEADER
        + ENUM_TABLE_BLURB
        + ENUM_TABLE
        + EXCEPTION_TABLE_HEADER
        + EXCEPTION_TABLE_BLURB
        + EXCEPTION_TABLE;
  }

  /**
   * Class that contains the information about an element of a class used to populate the tables in
   * the Package Overview file
   */
  public static class PackageChildSummary {
    String uid;
    String type;
    String summary;

    public PackageChildSummary(String uid, String type, String summary) {
      this.uid = uid;
      this.type = type;
      this.summary = summary;
    }

    private String getSummary() {
      return summary;
    }

    private String getType() {
      return type;
    }

    private String getUid() {
      return uid;
    }
  }

  /** Use to get the recommended package URL for Package Overview */
  private static String createHtmlTable(
      String type, String linkPrefix, List<PackageChildSummary> listOfPackageChildrenSummaries) {
    String tableHeader = type;
    if (type.equals("Client/Settings")) {
      tableHeader = "Clients or Settings Class";
    }
    StringBuilder tableBuilder = new StringBuilder();
    tableBuilder
        .append("<table>\n")
        .append("   <tr>\n")
        .append("     <th>\n")
        .append(tableHeader)
        .append("</th>\n")
        .append("     <th>\n")
        .append("Description")
        .append("</th>\n");

    for (PackageChildSummary packageChildSummary : listOfPackageChildrenSummaries) {
      if (packageChildSummary.type.equals(type)) {
        tableBuilder
            .append("<tr>\n")
            .append("<td><a href=\"")
            .append(linkPrefix + packageChildSummary.uid)
            .append("\">")
            .append(withLineBreaks(packageChildSummary.uid))
            .append("</a></td>\n")
            .append("<td>\n")
            .append(packageChildSummary.summary != null ? packageChildSummary.summary : "")
            .append("</td>\n")
            .append("   </tr>\n");
      }
    }
    tableBuilder.append(" </table>\n\n");
    return tableBuilder.toString();
  }

  /** Use to get the recommended package URL for Package Overview */
  public static ImmutableList<Object> extractPackageBaseURIBeforeVersion(
      String input, Pattern pattern) {
    Matcher matcher = pattern.matcher(input);
    boolean isVersioned = matcher.find();
    if (isVersioned) {
      ImmutableList<Object> packageBaseURIVersion =
          new ImmutableList.Builder<>().add(matcher.group(1)).add(matcher.group(2)).build();
      return packageBaseURIVersion;
    } else {
      ImmutableList<Object> packageBaseURIVersion =
          new ImmutableList.Builder<>().add("N/A").add("N/A").build();
      return packageBaseURIVersion;
    }
  }

  @JsonIgnore
  public String getFileNameWithPath() {
    return outputPath + File.separator + fileName;
  }

  @JsonIgnore
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @VisibleForTesting
  static String withLineBreaks(String uid) {
    Pattern p = Pattern.compile("[a-zA-Z\\d][a-z\\d]+\\.|[A-Z][a-z]+");
    Matcher m = p.matcher(uid);
    if (!m.find()) {
      return uid;
    }
    StringBuilder s = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(s, "<wbr>" + m.group());
    }
    return s.toString();
  }
}
