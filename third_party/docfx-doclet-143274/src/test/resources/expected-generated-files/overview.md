# google-cloud-apikeys overview (0.18.0)

## Key Reference Links
**API Keys API Description:** API Keys lets you create and manage your API keys for your projects.

<table>
   <tr>
     <td><a href="https://cloud.google.com/api-keys/">API Keys API Product Reference</a></td>
     <td><a href="https://github.com/googleapis/google-cloud-java/tree/main/java-apikeys">GitHub Repository (includes samples)</a></td>
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
implementation platform(&#39;com.google.cloud:libraries-bom:26.19.0&#39;)
implementation &#39;com.google.cloud:google-cloud-apikeys&#39;
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

## Which version should I use?
For this library, we recommend using API version v1 for new applications.

Each Cloud Java client library may contain multiple packages. Each package containing a version number in its name corresponds to a published version of the service.
We recommend using the latest stable version for new production applications, which can be identified by the largest numeric version that does not contain a suffix.
For example, if a client library has two packages: `v1` and `v2alpha`, then the latest stable version is `v1`.
If you use an unstable release, breaking changes may be introduced when upgrading.
You can read more about [Cloud API versioning strategy here](https://cloud.google.com/apis/design/versioning).

