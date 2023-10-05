package com.microsoft.build;

import static com.microsoft.build.BuilderUtil.LANGS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.docfx.doclet.RepoMetadata;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.util.Utils;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.PackageElement;

public class PackageOverviewFile {

  private String CLIENT_TABLE_HEADER = "";
  private String CLASSES_TABLE_HEADER = "";
  private String INTERFACES_TABLE_HEADER = "";
  private String ENUM_TABLE_HEADER = "";

  private String EXCEPTION_TABLE_HEADER = "";
  private String CLIENT_TABLE = "";

  private String CLASSES_TABLE = "";

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
              + "Each Cloud Java client library may contain multiple packages. Each package corresponds to a published version of the service.\n"
              + "We recommend using the latest stable version for new production applications, which can be identified by the largest numeric version that does not contain a suffix.\n"
              + "For example, if a client library has two packages: `v1` and `v2alpha`, then the latest stable version is `v1`.\n"
              + "If you use an unstable release, breaking changes may be introduced when upgrading.\n\n";
    }

    String basePackageURI = Utils.extractPackageBaseURIBeforeVersion(packageURIPath)[0];
    String packageVersion = Utils.extractPackageBaseURIBeforeVersion(packageURIPath)[1];
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

    MetadataFileItem packageItem =
        new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
    referenceBuilder.addChildrenSummaries(packageElement, packageItem.getChildrenSummaries());

    // Sort child by type and then by UID
    LinkedHashMap<String, String[]> sortedMap =
        packageItem.getChildrenSummaries().entrySet().stream()
            .sorted(
                Comparator.comparing(
                        (Map.Entry<String, String[]> e) -> e.getValue()[1]) // sort by type
                    .thenComparing(e -> e.getKey())) // then sort by UID
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    // Check if Client/Settings classes exist in this package
    boolean containsClientSettingsClasses =
        sortedMap.values().stream().anyMatch(values -> "Client/Settings".equals(values[1]));

    if (containsClientSettingsClasses) {
      this.CLIENT_TABLE_HEADER = "## Clients and Settings Classes\n";
      // Build table that contains Client and Settings classes
      StringBuilder clientTableBuilder = new StringBuilder();
      clientTableBuilder
          .append("<table>\n")
          .append("   <tr>\n")
          .append("     <th>\n")
          .append("Client or Settings Class")
          .append("</th>\n")
          .append("     <th>\n")
          .append("Description")
          .append("</th>\n");

      for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
        if (entry.getValue()[1] == "Client/Settings") {
          clientTableBuilder
              .append("<tr>\n")
              .append("<td><a href=\"")
              .append(cloudRADChildElementLinkPrefix + entry.getKey()) // Link to class
              .append("\">")
              .append(entry.getKey()) // class name
              .append("</a></td>\n")
              .append("<td>\n")
              .append(entry.getValue()[0] != null ? entry.getValue()[0] : "") // Description
              .append("</td>\n")
              .append("   </tr>\n");
        }
      }

      clientTableBuilder.append(" </table>\n\n");
      this.CLIENT_TABLE = clientTableBuilder.toString();
    }

    // Check if Classes exist in this package
    boolean containsClasses =
        sortedMap.values().stream().anyMatch(values -> "Class".equals(values[1]));

    if (containsClasses) {
      this.CLASSES_TABLE_HEADER = "## Classes\n";
      // Build table containing Classes
      StringBuilder classesTableBuilder = new StringBuilder();
      classesTableBuilder
          .append("<table>\n")
          .append("   <tr>\n")
          .append("     <th>\n")
          .append("Class")
          .append("</th>\n")
          .append("     <th>\n")
          .append("Description")
          .append("</th>\n");

      for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
        if (entry.getValue()[1] == "Class") {
          classesTableBuilder
              .append("<tr>\n")
              .append("<td><a href=\"")
              .append(cloudRADChildElementLinkPrefix + entry.getKey()) // Link to class
              .append("\">")
              .append(entry.getKey()) // class name
              .append("</a></td>\n")
              .append("<td>\n")
              .append(entry.getValue()[0] != null ? entry.getValue()[0] : "") // Description
              .append("</td>\n")
              .append("   </tr>\n");
        }
      }
      classesTableBuilder.append(" </table>\n\n");
      this.CLASSES_TABLE = classesTableBuilder.toString();
    }
    // Check if Interfaces exist in this package
    boolean containsInterfaces =
        sortedMap.values().stream().anyMatch(values -> "Interface".equals(values[0]));

    if (containsInterfaces) {
      this.INTERFACES_TABLE_HEADER = "## Interfaces\n";
      // Build table containing Interfaces
      StringBuilder interfacesTableBuilder = new StringBuilder();
      interfacesTableBuilder
          .append("<table>\n")
          .append("   <tr>\n")
          .append("     <th>\n")
          .append("Interface")
          .append("</th>\n")
          .append("     <th>\n")
          .append("Description")
          .append("</th>\n");

      for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
        if (entry.getValue()[1] == "Interface") {
          interfacesTableBuilder
              .append("<tr>\n")
              .append("<td><a href=\"")
              .append(cloudRADChildElementLinkPrefix + entry.getKey()) // Link to class
              .append("\">")
              .append(entry.getKey()) // class name
              .append("</a></td>\n")
              .append("<td>\n")
              .append(entry.getValue()[0] != null ? entry.getValue()[0] : "") // Description
              .append("</td>\n")
              .append("   </tr>\n");
        }
      }
      interfacesTableBuilder.append(" </table>\n\n");
      this.INTERFACES_TABLE = interfacesTableBuilder.toString();
    }

    // Check if Enums exist in this package
    boolean containsEnums =
        sortedMap.values().stream().anyMatch(values -> "Enum".equals(values[1]));

    if (containsEnums) {
      this.ENUM_TABLE_HEADER = "## Enums\n";
      // Build table containing Enums
      StringBuilder enumsTableBuilder = new StringBuilder();
      enumsTableBuilder
          .append("<table>\n")
          .append("   <tr>\n")
          .append("     <th>\n")
          .append("Enum")
          .append("</th>\n")
          .append("     <th>\n")
          .append("Description")
          .append("</th>\n");

      for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
        if (entry.getValue()[1] == "Enum") {
          enumsTableBuilder
              .append("<tr>\n")
              .append("<td><a href=\"")
              .append(cloudRADChildElementLinkPrefix + entry.getKey()) // Link to class
              .append("\">")
              .append(entry.getKey()) // class name
              .append("</a></td>\n")
              .append("<td>\n")
              .append(entry.getValue()[0] != null ? entry.getValue()[0] : "") // Description
              .append("</td>\n")
              .append("   </tr>\n");
        }
      }
      enumsTableBuilder.append(" </table>\n\n");
      this.ENUM_TABLE = enumsTableBuilder.toString();
    }

    // Check if Exceptions exist in this package
    boolean containsExceptions =
        sortedMap.values().stream().anyMatch(values -> "Exception".equals(values[1]));

    if (containsExceptions) {
      this.EXCEPTION_TABLE_HEADER = "## Exceptions\n";
      // Build table that contains Client and Settings classes
      StringBuilder exceptionTableBuilder = new StringBuilder();
      exceptionTableBuilder
          .append("<table>\n")
          .append("   <tr>\n")
          .append("     <th>\n")
          .append("Exceptions")
          .append("</th>\n")
          .append("     <th>\n")
          .append("Description")
          .append("</th>\n");

      for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
        if (entry.getValue()[1] == "Exception") {
          exceptionTableBuilder
              .append("<tr>\n")
              .append("<td><a href=\"")
              .append(cloudRADChildElementLinkPrefix + entry.getKey()) // Link to class
              .append("\">")
              .append(entry.getKey()) // class name
              .append("</a></td>\n")
              .append("<td>\n")
              .append(entry.getValue()[0] != null ? entry.getValue()[0] : "") // Description
              .append("</td>\n")
              .append("   </tr>\n");
        }
      }

      exceptionTableBuilder.append(" </table>\n\n");
      this.EXCEPTION_TABLE = exceptionTableBuilder.toString();
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
        + CLIENT_TABLE
        + CLASSES_TABLE_HEADER
        + CLASSES_TABLE
        + INTERFACES_TABLE_HEADER
        + INTERFACES_TABLE
        + ENUM_TABLE_HEADER
        + ENUM_TABLE
        + EXCEPTION_TABLE_HEADER
        + EXCEPTION_TABLE;
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
