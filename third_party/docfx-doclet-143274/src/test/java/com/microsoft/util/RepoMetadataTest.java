package com.microsoft.util;

import static junit.framework.TestCase.assertEquals;
import com.google.docfx.doclet.RepoMetadata;
import org.junit.Test;

public class RepoMetadataTest {
  @Test
  public void testParseRepoMetadata() {
    RepoMetadata testRepoMetadata = new RepoMetadata();
    String testRepoMetadataFilePath = "./src/test/java/com/microsoft/util/.repo-metadata.json";
    testRepoMetadata = testRepoMetadata.parseRepoMetadata(testRepoMetadataFilePath);
    assertEquals("translate", testRepoMetadata.getApiShortName());
    assertEquals("Cloud Translation", testRepoMetadata.getNamePretty());
    assertEquals("https://cloud.google.com/translate/docs/", testRepoMetadata.getProductDocumentationUri());
    assertEquals("can dynamically translate text between thousands of language pairs. Translation lets websites and programs programmatically integrate with the translation service.", testRepoMetadata.getApiDescription());
    assertEquals("https://cloud.google.com/java/docs/reference/google-cloud-translate/latest/overview", testRepoMetadata.getClientDocumentationUri());
    assertEquals("googleapis/google-cloud-java", testRepoMetadata.getRepo());
    assertEquals("java-translate", testRepoMetadata.getRepoShort());
    assertEquals("com.google.cloud:google-cloud-translate", testRepoMetadata.getDistributionName());
    assertEquals("translation.googleapis.com", testRepoMetadata.getApiId());
    assertEquals("google-cloud-translate", testRepoMetadata.getArtifactId());
    assertEquals("https://github.com/googleapis/google-cloud-java/tree/main/java-translate", testRepoMetadata.getGithubLink());
    assertEquals("https://central.sonatype.com/artifact/com.google.cloud/google-cloud-translate", testRepoMetadata.getMavenLink());
  }

}
