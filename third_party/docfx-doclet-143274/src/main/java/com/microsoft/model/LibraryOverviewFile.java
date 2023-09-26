package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.docfx.doclet.RepoMetadata;
import java.io.File;
import java.util.Arrays;

public class LibraryOverviewFile {
  // This is passed in as an environment variable
  private String repoMetadataFilePath;

  private final String outputPath;
  private String fileName;

  // This is passed in as an environment variable
  private String artifactVersion;
  // This is passed in as an environment variable
  private String librariesBomVersion;

  // This is parsed from the packages
  private String recommendedApiVersion;

  private String LIBRARY_OVERVIEW_FILE_HEADER;

  private String LIBRARY_OVERVIEW_KEY_REFERENCE_HEADER;

  private String LIBRARY_OVERVIEW_KEY_REFERENCE_TABLE;

  private String LIBRARY_OVERVIEW_GETTING_STARTED_SECTION;

  private String LIBRARY_OVERVIEW_CLIENT_INSTALLATION_SECTION;

  private String LIBRARY_OVERVIEW_CLIENT_INSTALLATION_HEADER;

  private String LIBRARY_OVERVIEW_PACKAGE_SELECTION_SECTION;

  public LibraryOverviewFile(
      String outputPath,
      String fileName,
      String artifactVersion,
      String librariesBomVersion,
      String repoMetadataFilePath,
      String recommendedApiVersion) {
    this.outputPath = outputPath;
    this.fileName = fileName;
    this.artifactVersion = artifactVersion;
    this.librariesBomVersion = librariesBomVersion;
    this.repoMetadataFilePath = repoMetadataFilePath;
    this.recommendedApiVersion = recommendedApiVersion;

    RepoMetadata repoMetadata = new RepoMetadata();
    repoMetadata = repoMetadata.parseRepoMetadata(this.repoMetadataFilePath);

    this.LIBRARY_OVERVIEW_FILE_HEADER =
        "# " + repoMetadata.getArtifactId() + " overview (" + artifactVersion + ")\n\n";

    this.LIBRARY_OVERVIEW_KEY_REFERENCE_HEADER =
        "## Key Reference Links\n" + repoMetadata.getApiDescription() + "\n\n";

    this.LIBRARY_OVERVIEW_KEY_REFERENCE_TABLE =
        "<table>\n"
            + "   <tr>\n"
            + "     <td><a href=\""
            + repoMetadata.getClientDocumentationUri()
            + "\">"
            + repoMetadata.getNamePretty()
            + " product reference</a></td>\n"
            + "     <td><a href=\""
            + repoMetadata.getGithubLink()
            + "\">Github repository (includes samples)</a></td>\n"
            + "     <td><a href=\""
            + repoMetadata.getMavenLink()
            + "\">Maven artifact</a></td>\n"
            + "   </tr>\n"
            + " </table>"
            + "\n\n";

    // For non-service libraries, these steps are not necessary
    String[] runtimeLibraries = {"gax", "api-common", "common-protos", "google-cloud-core"};

    if (Arrays.asList(runtimeLibraries).contains(repoMetadata.getApiShortName())) {
      this.LIBRARY_OVERVIEW_GETTING_STARTED_SECTION =
          "## Getting Started\n"
              + "In order to use this library, you first need to go through the following steps:"
              + "\n\n"
              + "- [Install a JDK (Java Development Kit)](https://cloud.google.com/java/docs/setup#install_a_jdk_java_development_kit)\n\n";
    } else {
      this.LIBRARY_OVERVIEW_GETTING_STARTED_SECTION =
          "## Getting Started\n"
              + "In order to use this library, you first need to go through the following steps:"
              + "\n\n"
              + "- [Install a JDK (Java Development Kit)](https://cloud.google.com/java/docs/setup#install_a_jdk_java_development_kit)\n"
              + "- [Select or create a Cloud Platform project](https://console.cloud.google.com/project)\n"
              + "- [Enable billing for your project](\"https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_a_project)\n"
              + "- [Enable the API](https://console.cloud.google.com/apis/library/"
              + repoMetadata.getApiShortName()
              + ".googleapis.com)\n"
              + "- [Set up authentication](https://cloud.google.com/docs/authentication/client-libraries)\n\n";
    }

    this.LIBRARY_OVERVIEW_CLIENT_INSTALLATION_HEADER =
        "## Use the "
            + repoMetadata.getNamePretty()
            + " for Java\n"
            + "To ensure that your project uses compatible versions of the libraries\n"
            + "and their component artifacts, import `com.google.cloud:libraries-bom` and use\n"
            + "the BOM to specify dependency versions.  Be sure to remove any versions that you\n"
            + "set previously. For more information about\n"
            + "BOMs, see [Google Cloud Platform Libraries BOM](https://cloud.google.com/java/docs/bom).\n\n";

    this.LIBRARY_OVERVIEW_CLIENT_INSTALLATION_SECTION =
        "<div>\n"
            + "<devsite-selector>\n"
            + "<section>\n"
            + "<h3>Maven</h3>\n"
            + "<p>Import the BOM in the <code>dependencyManagement</code> section of your <code>pom.xml</code> file.\n"
            + "Include specific artifacts you depend on in the <code>dependencies</code> section, but don't\n"
            + "specify the artifacts' versions in the <code>dependencies</code> section.</p>\n"
            + "\n"
            + "<p>The example below demonstrates how you would import the BOM and include the <code>google-cloud-apikeys</code>\n"
            + "artifact.</p>\n"
            + "<pre class=\"prettyprint lang-xml devsite-click-to-copy\">\n"
            + "&lt;dependencyManagement&gt;\n"
            + " &lt;dependencies&gt;\n"
            + "   &lt;dependency&gt;\n"
            + "      &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;\n"
            + "      &lt;artifactId&gt;libraries-bom&lt;/artifactId&gt;\n"
            + "      &lt;version&gt;"
            + this.librariesBomVersion
            + "&lt;/version&gt;\n"
            + "      &lt;type&gt;pom&lt;/type&gt;\n"
            + "      &lt;scope&gt;import&lt;/scope&gt;\n"
            + "   &lt;/dependency&gt;\n"
            + " &lt;/dependencies&gt;\n"
            + "&lt;/dependencyManagement&gt;\n\n"
            + "&lt;dependencies&gt;\n"
            + " &lt;dependency&gt;\n"
            + "   &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;\n"
            + "   &lt;artifactId&gt;"
            + repoMetadata.getArtifactId()
            + "&lt;/artifactId&gt;\n"
            + " &lt;/dependency&gt;\n"
            + "&lt;/dependencies&gt;\n"
            + "</pre>\n"
            + "</section>\n"
            + "<section>\n"
            + "<h3>Gradle</h3>\n"
            + "<p>BOMs are supported by default in Gradle 5.x or later. Add a <code>platform</code>\n"
            + "dependency on <code>com.google.cloud:libraries-bom</code> and remove the version from the\n"
            + "dependency declarations in the artifact's <code>build.gradle</code> file.</p>\n"
            + "\n"
            + "<p>The example below demonstrates how you would import the BOM and include the <code>google-cloud-apikeys</code>\n"
            + "artifact.</p>\n"
            + "<pre class=\"prettyprint lang-Groovy devsite-click-to-copy\">\n"
            + "implementation platform(&#39;com.google.cloud:libraries-bom:"
            + librariesBomVersion
            + "&#39;)\n"
            + "implementation &#39;"
            + repoMetadata.getDistributionName()
            + "&#39;\n"
            + "</pre>\n"
            + "<p>The <code>platform</code> and <code>enforcedPlatform</code> keywords supply dependency versions\n"
            + "declared in a BOM. The <code>enforcedPlatform</code> keyword enforces the dependency\n"
            + "versions declared in the BOM and thus overrides what you specified.</p>\n"
            + "\n"
            + "<p>For more details of the <code>platform</code> and <code>enforcedPlatform</code> keywords Gradle 5.x or higher, see\n"
            + "<a href=\"https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import\">Gradle: Importing Maven BOMs</a>.</p>\n"
            + "\n"
            + "<p>If you're using Gradle 4.6 or later, add\n"
            + "<code>enableFeaturePreview('IMPROVED_POM_SUPPORT')</code> to your <code>settings.gradle</code> file. For details, see\n"
            + "<a href=\"https://docs.gradle.org/4.6/release-notes.html#bom-import\">Gradle 4.6 Release Notes: BOM import</a>.\n"
            + "Versions of Gradle earlier than 4.6 don't support BOMs.</p>\n"
            + "</section>\n"
            + "<section>\n"
            + "<h3>SBT</h3>\n"
            + "<p>SBT <a href=\"https://github.com/sbt/sbt/issues/4531\">doesn't support BOMs</a>. You can find\n"
            + "recommended versions of libraries from a particular BOM version on the\n"
            + "<a href=\"https://storage.googleapis.com/cloud-opensource-java-dashboard/com.google.cloud/libraries-bom/index.html\">dashboard</a>\n"
            + "and set the versions manually.</p>\n"
            + "<p>To use the latest version of this library, add this to your dependencies:</p>\n"
            + "<pre class=\"prettyprint lang-Scala devsite-click-to-copy\">\n"
            + "libraryDependencies += &quot;com.google.cloud&quot; % &quot;"
            + repoMetadata.getArtifactId()
            + "&quot; % &quot;"
            + artifactVersion
            + "&quot;\n"
            + "</pre>\n"
            + "</section>\n"
            + "</devsite-selector>\n"
            + "</div>\n\n";

    this.LIBRARY_OVERVIEW_PACKAGE_SELECTION_SECTION =
        "## Which version should I use?\n"
            + "For this library, we recommend using API version "
            // TODO: @alicejli determine best way to pull in the link to the package for this
            + this.recommendedApiVersion
            + " for new applications.\n"
            + "\n"
            + "Each Cloud Java client library may contain multiple packages. Each package corresponds to a published version of the service.\n"
            + "We recommend using the latest stable version for new production applications, which can be identified by the largest numeric version that does not contain a suffix.\n"
            + "For example, if a client library has two packages: `v1` and `v2alpha`, then the latest stable version is `v1`.\n"
            + "If you use an unstable release, breaking changes may be introduced when upgrading.\n\n";
  }

  @JsonIgnore
  public String getFileContent() {
    return LIBRARY_OVERVIEW_FILE_HEADER
        + LIBRARY_OVERVIEW_KEY_REFERENCE_HEADER
        + LIBRARY_OVERVIEW_KEY_REFERENCE_TABLE
        + LIBRARY_OVERVIEW_GETTING_STARTED_SECTION
        + LIBRARY_OVERVIEW_CLIENT_INSTALLATION_HEADER
        + LIBRARY_OVERVIEW_CLIENT_INSTALLATION_SECTION
        + LIBRARY_OVERVIEW_PACKAGE_SELECTION_SECTION;
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
