package com.microsoft.doclet;

import com.microsoft.util.FileUtilTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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
        DocletRunner.main(new String[]{});

        assertEquals("Wrong System.err content",
            errContent.toString().trim(), "Usage: java DocletRunner <doclet-params-filename>");
    }

    @Test
    public void testFilesGenerationWhenTargetFileDoesNotExist() {
        DocletRunner.main(new String[]{"some-name.txt"});

        assertEquals("Wrong System.err content",
            errContent.toString().trim(),"File 'some-name.txt' does not exist");
    }

    @Test
    public void testFilesGeneration() throws IOException {
        DocletRunner.main(new String[]{PARAMS_DIR});

        List<Path> expectedFilePaths = Files.list(Path.of(EXPECTED_GENERATED_FILES_DIR)).collect(Collectors.toList());
        List<Path> generatedFilePaths = Files.list(Path.of(OUTPUT_DIR)).collect(Collectors.toList());
        assertEquals("Wrong files count", generatedFilePaths.size(), expectedFilePaths.size());

        for (Path expectedFilePath : expectedFilePaths) {
            Path generatedFilePath = Path.of(OUTPUT_DIR, expectedFilePath.getFileName().toString());

            String generatedFileContent = Files.readString(generatedFilePath);
            String expectedFileContent = Files.readString(expectedFilePath);

            String[] generatedFileLines = generatedFileContent.split("\n");
            String[] expectedFileLines = expectedFileContent.split("\n");

            assertEquals("Unexpected amount of lines in file " + generatedFilePath, generatedFileLines.length,
                expectedFileLines.length);

            for (int i = 0; i < generatedFileLines.length; i++) {
                assertEquals("Wrong file content for file " + generatedFilePath,
                    generatedFileLines[i], expectedFileLines[i]);
            }
        }
    }
}
