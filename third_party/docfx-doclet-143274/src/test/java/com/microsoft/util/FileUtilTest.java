package com.microsoft.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileUtilTest {

    private final String ROOT_DIR = "target/dir1";
    private final String FILE_NAME = ROOT_DIR + "/dir2/out.txt";

    @Before
    public void setup() throws IOException {
        deleteDirectory(ROOT_DIR);
    }

    @After
    public void tearDown() throws IOException {
        deleteDirectory(ROOT_DIR);
    }

    @Test
    public void dumpToFileWithDirectoryCreation() throws IOException {
        String content = "Bla-bla content";

        FileUtil.dumpToFile(content, FILE_NAME);

        assertTrue("New file should appear", Files.exists(Paths.get(FILE_NAME)));
        assertEquals("Invalid file content", Files.readString(Paths.get(FILE_NAME)), content);
    }

    @Test
    public void dumpToFileForExistingNonEmptyDirectory() throws IOException {
        createDirectoryWithFile(ROOT_DIR + "/dir2/tmp1.txt");
        String content = "Bla-bla content";

        FileUtil.dumpToFile(content, FILE_NAME);

        assertTrue("Existing file should not be deleted", Files.exists(Path.of(ROOT_DIR + "/dir2/tmp1.txt")));
        assertTrue("New file should appear", Files.exists(Paths.get(FILE_NAME)));
        assertEquals("Invalid file content", Files.readString(Paths.get(FILE_NAME)), content);
    }

    public static void deleteDirectory(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static void createDirectoryWithFile(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
    }
}
