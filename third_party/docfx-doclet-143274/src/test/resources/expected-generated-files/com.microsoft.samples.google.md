# Package com.microsoft.samples.google (0.18.0)
<table>
   <tr>
     <td><a href="https://github.com/googleapis/google-cloud-java/tree/main/java-apikeys/google-cloud-apikeys/src/main/java/com/microsoft/samples/google">GitHub Repository</a></td>
     <td><a href="https://cloud.google.com/api-keys/docs/reference/rpc">RPC Documentation</a></td>
     <td><a href="https://cloud.google.com/api-keys/docs/reference/rest">REST Documentation</a></td>
   </tr>
 </table>

## This package is not the recommended entry point to using this client library!

 For this library, we recommend using [com.microsoft.samples.google.v1](https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.v1) for new applications.

## Client Classes
Client classes are the main entry point to using a package.
They contain several variations of Java methods for each of the API's methods.
<table>
   <tr>
     <th>
Client</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.SpeechClient">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Speech<wbr>Client</a></td>
<td>
Service Description: Service that implements Google Cloud Speech API.

 <p>This class provides the ability to make remote calls to the backing service through method
 calls that map to API methods. Sample code to get started:</td>
   </tr>
 </table>

## Settings Classes
Settings classes can be used to configure credentials, endpoints, and retry settings for a Client.
<table>
   <tr>
     <th>
Settings</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.ProductSearchSettings">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Product<wbr>Search<wbr>Settings</a></td>
<td>
Settings class to configure an instance of <xref uid="ProductSearchClient" data-throw-if-not-resolved="false">ProductSearchClient</xref>.

 <p>The default instance has everything set to sensible defaults:
</td>
   </tr>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.SpeechSettings">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Speech<wbr>Settings</a></td>
<td>
Settings class to configure an instance of <xref uid="SpeechClient" data-throw-if-not-resolved="false">SpeechClient</xref>.

 <p>The default instance has everything set to sensible defaults:
</td>
   </tr>
 </table>

## Classes
<table>
   <tr>
     <th>
Class</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.ProductSearchSettings.Builder">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Product<wbr>Search<wbr>Settings.<wbr>Builder</a></td>
<td>
Builder for ProductSearchSettings.</td>
   </tr>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.RecognitionAudio">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Recognition<wbr>Audio</a></td>
<td>

 Contains audio data in the encoding specified in the <code>RecognitionConfig</code>.
 Either <code>content</code> or <code>uri</code> must be supplied. Supplying both or neither
 returns <xref uid="google.rpc.Code.INVALID_ARGUMENT" data-throw-if-not-resolved="false">google.rpc.Code.INVALID_ARGUMENT</xref>. See</td>
   </tr>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.SpeechSettings.Builder">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Speech<wbr>Settings.<wbr>Builder</a></td>
<td>
Builder for SpeechSettings.</td>
   </tr>
 </table>

## Interfaces
<table>
   <tr>
     <th>
Interface</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.BetaApi">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Beta<wbr>Api</a></td>
<td>
Indicates a public API that can change at any time, and has no guarantee of API stability and
 backward-compatibility.

 <p>Usage guidelines:</td>
   </tr>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.ValidationException.Supplier">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Validation<wbr>Exception.<wbr>Supplier</a></td>
<td>
</td>
   </tr>
 </table>

## Enums
<table>
   <tr>
     <th>
Enum</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.RecognitionAudio.AudioSourceCase">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Recognition<wbr>Audio.<wbr>Audio<wbr>Source<wbr>Case</a></td>
<td>
</td>
   </tr>
 </table>

## Exceptions
<table>
   <tr>
     <th>
Exception</th>
     <th>
Description</th>
<tr>
<td><a href="https://cloud.google.com/java/docs/reference/google-cloud-apikeys/latest/com.microsoft.samples.google.ValidationException">com.<wbr>microsoft.<wbr>samples.<wbr>google.<wbr>Validation<wbr>Exception</a></td>
<td>
Exception thrown if there is a validation problem with a path template, http config, or related
 framework methods. Comes as an illegal argument exception subclass. Allows to globally set a
 thread-local validation context description which each exception inherits.
See Also: <a href="https://cloud.google.com/storage/docs/json_api/v1/status-codes">Google Cloud  Storage error codes</a>
</td>
   </tr>
 </table>

