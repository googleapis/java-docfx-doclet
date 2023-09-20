package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.docfx.doclet.RepoMetadata;
import java.io.File;

public class LibraryOverviewFile implements YmlFile {
  // TODO: @alicejli Update to use generic .repo-metadata.json file if it exists
  String repoMetadataFileName =
      "/home/alicejli/java-docfx-doclet/third_party/docfx-doclet-143274/src/test/java/com/microsoft/samples/.repo-metadata.json";
  RepoMetadata repoMetadata = RepoMetadata.parseRepoMetadata(repoMetadataFileName);

  private final String LIBRARY_OVERVIEW_FILE_HEADER =
      "# " + repoMetadata.getArtifactId() + " overview\n\n";

  private final String LIBRARY_OVERVIEW_KEY_REFERENCE_HEADER =
      "## Key Reference Links\n" + repoMetadata.getApiDescription() + "\n\n";

  private final String LIBRARY_OVERVIEW_KEY_REFERENCE_TABLE =
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

  // TODO: @alicejli Update to exclude this section for runtime libraries (e.g. gax)
  private final String LIBRARY_OVERVIEW_GETTING_STARTED_SECTION =
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

  private final String LIBRARY_OVERVIEW_CLIENT_INSTALLATION_HEADER =
      "## Use the "
          + repoMetadata.getNamePretty()
          + " for Java\n"
          + "To ensure that your project uses compatible versions of the libraries\n"
          + "and their component artifacts, import `com.google.cloud:libraries-bom` and use\n"
          + "the BOM to specify dependency versions.  Be sure to remove any versions that you\n"
          + "set previously. For more information about\n"
          + "BOMs, see [Google Cloud Platform Libraries BOM](https://cloud.google.com/java/docs/bom).\n\n";

  private final String LIBRARY_OVERVIEW_CLIENT_INSTALLATION_SECTION =
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
          // TODO: @alicejli determine best way to pull in libraries-bom version for this
          + "      &lt;version&gt;"
          + "26.18.0"
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
          // TODO: @alicejli determine best way to pull in libraries-bom version for this
          + "implementation platform(&#39;com.google.cloud:libraries-bom:"
          + "26.18.0"
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
          // TODO: @alicejli determine best way to pull in artifact version for this
          + "libraryDependencies += &quot;com.google.cloud&quot; % &quot;"
          + repoMetadata.getArtifactId()
          + "&quot; % &quot;"
          + "0.18.0"
          + "&quot;\n"
          + "</pre>\n"
          + "</section>\n"
          + "</devsite-selector>\n"
          + "</div>\n\n";
  private final String LIBRARY_OVERVIEW_PACKAGE_SELECTION_SECTION =
      "## Which package should I use?\n"
          // TODO: @alicejli determine best way to pull in the link to the package for this
          + "The recommended package for new applications is ["
          + "com.google.api.apikeys.v2"
          + "](https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.google.api.apikeys.v2).\n"
          + "\n"
          + "Each Cloud Java client library may contain multiple packages. Each package corresponds to a published version of the service.\n"
          + "We recommend using the latest stable version for new production applications, which can be identified by the largest numeric version that does not contain a suffix.\n"
          + "For example, if a client library has two packages: `v1` and `v2alpha`, then the latest stable version is `v1`.\n"
          + "If you use an unstable release, breaking changes may be introduced when upgrading.\n\n";
  private final String outputPath;
  private String fileName;

  public LibraryOverviewFile(String outputPath, String fileName) {
    this.outputPath = outputPath;
    this.fileName = fileName;
  }

  @JsonIgnore
  @Override
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
  @Override
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
