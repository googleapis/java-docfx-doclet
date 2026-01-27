package com.google.docfx.doclet;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Test;

public class RepoMetadataTest {

  @Test
  public void testParseWithLibraryPathOverrides() {
    String json =
        "{ "
            + "\"distribution_name\": \"com.google.cloud:google-cloud-firestore\", "
            + "\"library_path_overrides\": { "
            + "  \"FirestoreAdminClient\": \"google-cloud-firestore-admin\" "
            + "}, "
            + "\"repo\": \"googleapis/java-firestore\" "
            + "}";

    RepoMetadata metadata = new Gson().fromJson(json, RepoMetadata.class);

    assertEquals("google-cloud-firestore", metadata.getArtifactId());
    // Verify the map is populated correctly
    assertEquals(
        "google-cloud-firestore-admin",
        metadata.getLibraryPathOverrides().get("FirestoreAdminClient"));
  }
}
