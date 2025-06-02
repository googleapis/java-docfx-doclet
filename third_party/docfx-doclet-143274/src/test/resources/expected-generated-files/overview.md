# google-cloud-apikeys overview (0.18.0)

## Key Reference Links
**API Keys API Description:** API Keys lets you create and manage your API keys for your projects.

<table>
   <tr>
     <td><a href="https://cloud.google.com/api-keys/">API Keys API Product Reference</a></td>
     <td><a href="https://github.com/googleapis/google-cloud-java/tree/main/java-apikeys">GitHub Repository</a></td>
     <td><a href="https://central.sonatype.com/artifact/com.google.cloud/google-cloud-apikeys">Maven artifact</a></td>
   </tr>
 </table>

## Getting Started
In order to use this library, you first need to go through the following steps:

- [Install a JDK (Java Development Kit)](https://cloud.google.com/java/docs/setup#install_a_jdk_java_development_kit)
- [Select or create a Cloud Platform project](https://console.cloud.google.com/project)
- [Enable billing for your project](https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_a_project)
- [Enable the API](https://console.cloud.google.com/apis/library/apikeys.googleapis.com)
- [Set up authentication](https://cloud.google.com/docs/authentication/client-libraries)

## Use the API Keys API for Java
To ensure that your project uses compatible versions of the libraries
and their component artifacts, import `com.google.cloud:libraries-bom` and use
the BOM to specify dependency versions.  Be sure to remove any versions that you
set previously. For more information about
BOMs, see [Google Cloud Platform Libraries BOM](https://cloud.google.com/java/docs/bom).

### Maven
Import the BOM in the <code>dependencyManagement</code> section of your <code>pom.xml</code> file.
Include specific artifacts you depend on in the <code>dependencies</code> section, but don't
specify the artifacts' versions in the <code>dependencies</code> section.

The example below demonstrates how you would import the BOM and include the <code>google-cloud-apikeys</code> artifact.
<pre class="prettyprint lang-xml devsite-click-to-copy">
&lt;dependencyManagement&gt;
 &lt;dependencies&gt;
   &lt;dependency&gt;
      &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;
      &lt;artifactId&gt;libraries-bom&lt;/artifactId&gt;
      &lt;version&gt;26.19.0&lt;/version&gt;
      &lt;type&gt;pom&lt;/type&gt;
      &lt;scope&gt;import&lt;/scope&gt;
   &lt;/dependency&gt;
 &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;

&lt;dependencies&gt;
 &lt;dependency&gt;
   &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;
   &lt;artifactId&gt;google-cloud-apikeys&lt;/artifactId&gt;
 &lt;/dependency&gt;
&lt;/dependencies&gt;
</pre>

### Gradle
BOMs are supported by default in Gradle 5.x or later. Add a <code>platform</code>
dependency on <code>com.google.cloud:libraries-bom</code> and remove the version from the
dependency declarations in the artifact's <code>build.gradle</code> file.

The example below demonstrates how you would import the BOM and include the <code>google-cloud-apikeys</code> artifact.
<pre class="prettyprint lang-Groovy devsite-click-to-copy">
implementation(platform(&quot;com.google.cloud:libraries-bom:26.19.0&quot;))
implementation(&quot;com.google.cloud:google-cloud-apikeys&quot;)
</pre>

The <code>platform</code> and <code>enforcedPlatform</code> keywords supply dependency versions
declared in a BOM. The <code>enforcedPlatform</code> keyword enforces the dependency
versions declared in the BOM and thus overrides what you specified.

For more details of the <code>platform</code> and <code>enforcedPlatform</code> keywords Gradle 5.x or higher, see
[Gradle: Importing Maven BOMs](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import).

If you're using Gradle 4.6 or later, add
<code>enableFeaturePreview('IMPROVED_POM_SUPPORT')</code> to your <code>settings.gradle</code> file. For details, see
[Gradle 4.6 Release Notes: BOM import](https://docs.gradle.org/4.6/release-notes.html#bom-import).
Versions of Gradle earlier than 4.6 don't support BOMs.</p>

### SBT
SBT [doesn't support BOMs](https://github.com/sbt/sbt/issues/4531). You can find
recommended versions of libraries from a particular BOM version on the
[dashboard](https://storage.googleapis.com/cloud-opensource-java-dashboard/com.google.cloud/libraries-bom/index.html)
and set the versions manually.
To use the latest version of this library, add this to your dependencies:
<pre class="prettyprint lang-Scala devsite-click-to-copy">
libraryDependencies += &quot;com.google.cloud&quot; % &quot;google-cloud-apikeys&quot; % &quot;0.18.0&quot;
</pre>

## Which version ID should I get started with?
For this library, we recommend using [com.microsoft.samples.google.v1](https://cloud.google.com/java/docs/reference/google-cloud-apikeys/0.18.0/com.microsoft.samples.google.v1) for new applications.

### Understanding Version ID and Library Versions
When using a Cloud client library, it's important to distinguish between two types of versions:
- **Library Version**: The version of the software package (the client library) that helps you interact with the Cloud service. These libraries are
released and updated frequently with bug fixes, improvements, and support for new service features and versions. The version selector at
the top of this page represents the client library version.
- **Version ID**: The version of the Cloud service itself (e.g. API Keys API). New Version IDs are introduced infrequently, and often involve
changes to the core functionality and structure of the Cloud service itself. The packages in the lefthand navigation represent packages tied
to a specific Version ID of the Cloud service.

### Managing Library Versions
We recommend using the <code>com.google.cloud:libraries-bom</code> installation method detailed above to streamline dependency management
across multiple Cloud Java client libraries. This ensures compatibility and simplifies updates.

### Choosing the Right Version ID
Each Cloud Java client library may contain packages tied to specific Version IDs (e.g., <code>v1</code>, <code>v2alpha</code>). For new production applications, use
the latest stable Version ID. This is identified by the highest version number **without** a suffix (like "alpha" or "beta"). You can read more about
[Cloud API versioning strategy here](https://cloud.google.com/apis/design/versioning).

**Important**: Unstable Version ID releases (those _with_ suffixes) are subject to breaking changes when upgrading. Use them only for testing or if you specifically need their experimental features.

