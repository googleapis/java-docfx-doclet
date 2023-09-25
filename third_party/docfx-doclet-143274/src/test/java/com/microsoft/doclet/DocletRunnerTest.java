package com.microsoft.doclet;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.microsoft.util.FileUtilTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class DocletRunnerTest {

  private final String PARAMS_DIR = "src/test/resources/test-doclet-params.txt";
  private final String EXPECTED_GENERATED_FILES_DIR = "src/test/resources/expected-generated-files";
  private final String OUTPUT_DIR = "target/test-out";

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @Before
  public void cleanup() throws IOException {
    FileUtilTest.deleteDirectory(OUTPUT_DIR);

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testFilesGenerationWhenNoParams() {
    DocletRunner.main(new String[] {});

    assertEquals(
        "Wrong System.err content",
        errContent.toString().trim(),
        "Usage: java DocletRunner <options file> <argfile>");
  }

  @Test
  public void testFilesGenerationWhenTargetFileDoesNotExist() {
    try {
      DocletRunner.main(new String[] {"some-name.txt"});
      fail();
    } catch (RuntimeException ex) {
      assertEquals(
          "Wrong System.err content",
          errContent.toString().trim(),
          "File 'some-name.txt' does not exist");
    }
  }

  @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Test
  public void testFilesGeneration() throws IOException {
    environmentVariables.set("artifactVersion", "0.18.0");
    environmentVariables.set("librariesBomVersion", "26.19.0");
    environmentVariables.set(
        "repoMetadataFilePath", "./src/test/java/com/microsoft/samples/.repo-metadata.json");
    assertEquals("0.18.0", System.getenv("artifactVersion"));
    assertEquals("26.19.0", System.getenv("librariesBomVersion"));
    assertEquals(
        "./src/test/java/com/microsoft/samples/.repo-metadata.json",
        System.getenv("repoMetadataFilePath"));

    DocletRunner.main(new String[] {PARAMS_DIR});

    List<Path> expectedFilePaths =
        Files.list(Path.of(EXPECTED_GENERATED_FILES_DIR)).sorted().collect(Collectors.toList());
    List<Path> generatedFilePaths =
        Files.list(Path.of(OUTPUT_DIR)).sorted().collect(Collectors.toList());

    assertSameFileNames(expectedFilePaths, generatedFilePaths);

    for (Path expectedFilePath : expectedFilePaths) {
      Path generatedFilePath = Path.of(OUTPUT_DIR, expectedFilePath.getFileName().toString());

      String generatedFileContent = Files.readString(generatedFilePath);
      String expectedFileContent = Files.readString(expectedFilePath);

      String[] generatedFileLines = generatedFileContent.split("\n");
      String[] expectedFileLines = expectedFileContent.split("\n");

      assertEquals(
          "Unexpected amount of lines in file " + generatedFilePath,
          generatedFileLines.length,
          expectedFileLines.length);

      for (int i = 0; i < generatedFileLines.length; i++) {
        assertEquals(
            "Wrong file content for file " + generatedFilePath,
            expectedFileLines[i],
            generatedFileLines[i]);
      }
    }
    environmentVariables.clear("artifactVersion");
    environmentVariables.clear("librariesBomVersion");
    environmentVariables.clear("repoMetadataFilePath");
    assertNull(System.getenv("artifactVersion"));
    assertNull(System.getenv("librariesBomVersion"));
    assertNull(System.getenv("repoMetadataFilePath"));
  }

  public void assertSameFileNames(List<Path> expected, List<Path> generated) {
    List<String> expectedFilenames =
        expected.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.toList());
    List<String> generatedFilenames =
        generated.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.toList());

    assertWithMessage("Expected files were not generated.")
        .that(
            expectedFilenames.stream()
                .filter(file -> !generatedFilenames.contains(file))
                .collect(Collectors.toList()))
        .isEmpty();

    assertWithMessage("Files were generated that should not have been.")
        .that(
            generatedFilenames.stream()
                .filter(file -> !expectedFilenames.contains(file))
                .collect(Collectors.toList()))
        .isEmpty();

    assertThat(expected.size()).isEqualTo(generated.size());
  }
}
