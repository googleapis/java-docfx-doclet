package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.LANGS;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private String recommendedApiVersion;

  // This is only set if the package is not the latest GA package
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
      String recommendedApiVersion) {
    this.outputPath = outputPath;
    this.fileName = fileName;
    this.packageElement = packageElement;
    this.recommendedApiVersion = recommendedApiVersion;

    String packageURIPath = fileName.replace(".md", "");

    this.PACKAGE_HEADER = "# Package " + packageURIPath + " (" + artifactVersion + ")\n";

    // This will always link to the latest version of the package classes
    String cloudRADChildElementLinkPrefix =
        "https://cloud.google.com/java/docs/reference/" + repoMetadata.getArtifactId() + "/latest/";

    String packageURIPathGithub = packageURIPath.replace('.', '/');
    String githubSourcePackageLink =
        repoMetadata.getGithubLink()
            + "/"
            + repoMetadata.getArtifactId()
            + "/src/main/java/"
            + packageURIPathGithub;

    // If the package status is not a GA version, then add a disclaimer around prerelease
    // implications
    if (status != null) {
      this.PRERELEASE_IMPLICATIONS =
          "## Prerelease Implications\n\n"
              + "This package is a prerelease version! Use with caution.\n"
              + "Prerelease versions are considered unstable as they may be shut down. You can read more about [Cloud API versioning strategy here](https://cloud.google.com/apis/design/versioning).\n"
              + "Each Cloud Java client library may contain multiple packages. Each package containing a version number in its name corresponds to a published version of the service.\n"
              + "We recommend using the latest stable version for new production applications, which can be identified by the largest numeric version that does not contain a suffix.\n"
              + "For example, if a client library has two packages: `v1` and `v2alpha`, then the latest stable version is `v1`.\n"
              + "If you use an unstable release, breaking changes may be introduced when upgrading.\n\n";
    }

    // This regex captures the package path before the version (if it exists) and the version
    Pattern pattern = Pattern.compile("(.*?)(v\\d+.*?)(?:\\.|$)");
    ImmutableList<Object> packageVersionURIInfo =
        extractPackageBaseURIBeforeVersion(packageURIPath, pattern);
    String basePackageURI = packageVersionURIInfo.get(0).toString();
    String packageVersion = packageVersionURIInfo.get(1).toString();
    // Build link to the Cloud RAD link for the recommended package
    String recommendedPackageVersionLink =
        cloudRADChildElementLinkPrefix + basePackageURI + recommendedApiVersion;

    // A package is not the latest GA version if it is a prerelease version, or if it is a GA
    // version that is not the same as the recommended Api version
    if (basePackageURI != "N/A") {
      if (status != null || (!packageVersion.equals(this.recommendedApiVersion))) {
        this.RECOMMENDED_VERSION =
            "## This package is not the latest GA version! \n\n"
                + " For this library, we recommend using the [package]("
                + recommendedPackageVersionLink
                + ")"
                + " associated with API version "
                + this.recommendedApiVersion
                + " for new applications.\n"
                + "\n";
      }
    }

    // If recommended version package URI exists, link to it for the Stub class as well
    if (basePackageURI != "N/A"
        && String.valueOf(this.packageElement.getQualifiedName()).contains("stub")) {
      this.STUB_IMPLICATIONS =
          "## Stub Package Implications\n\n"
              + "This package is a a base stub class. It is for advanced usage and reflects the underlying API directly.\n"
              + "We generally recommend using non-stub, latest GA package, such as ["
              + basePackageURI
              + recommendedApiVersion
              + "]("
              + recommendedPackageVersionLink
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
        .append("\">Github repository</a></td>\n");

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
      this.SETTINGS_TABLE_BLURB =
          "Settings classes can be used to configure credentials, endpoints, and retry settings for a Client.\n";
      this.SETTINGS_TABLE =
          createHtmlTable(
              "Settings", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If Stubs exist in this package, create a table of them
    boolean containsStubClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(packageChildSummary -> "Stub".equals(packageChildSummary.getType()));
    if (containsStubClasses) {
      this.STUB_TABLE_HEADER = "## Stub Classes\n";
      this.STUB_TABLE_BLURB = "";
      this.STUB_TABLE =
          createHtmlTable("Stub", cloudRADChildElementLinkPrefix, listOfPackageChildrenSummaries);
    }

    // If Callable Factory classes exist in this package, create a table of them
    boolean containsCallableFactoryClasses =
        listOfPackageChildrenSummaries.stream()
            .anyMatch(
                packageChildSummary -> "CallableFactory".equals(packageChildSummary.getType()));
    if (containsCallableFactoryClasses) {
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
    if (type == "Client/Settings") {
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
      if (packageChildSummary.type == type) {
        tableBuilder
            .append("<tr>\n")
            .append("<td><a href=\"")
            .append(linkPrefix + packageChildSummary.uid)
            .append("\">")
            .append(packageChildSummary.uid)
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
}
