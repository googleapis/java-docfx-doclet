package com.microsoft.util;

import com.microsoft.build.PackageOverviewFile;
import com.microsoft.model.LibraryOverviewFile;
import com.microsoft.model.YmlFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

  /** Dump string to file. Create required folders when needed */
  public static void dumpToFile(String content, String fileName) {

    try {
      Path path = Paths.get(fileName);
      Files.createDirectories(path.getParent());
      Files.write(path, content.getBytes());
    } catch (IOException ioe) {
      throw new RuntimeException("Error during dump to file", ioe);
    }
  }

  public static void dumpToFile(YmlFile ymlFile) {
    dumpToFile(ymlFile.getFileContent(), ymlFile.getFileNameWithPath());
  }

  public static void dumpToFile(LibraryOverviewFile libraryOverviewFile) {
    dumpToFile(libraryOverviewFile.getFileContent(), libraryOverviewFile.getFileNameWithPath());
  }

  public static void dumpToFile(PackageOverviewFile packageOverviewFile) {
    dumpToFile(packageOverviewFile.getFileContent(), packageOverviewFile.getFileNameWithPath());
  }
}
