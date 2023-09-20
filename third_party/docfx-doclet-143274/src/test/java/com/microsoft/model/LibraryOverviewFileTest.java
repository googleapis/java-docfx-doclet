package com.microsoft.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LibraryOverviewFileTest {

  @Test
  public void testEquals() {
    LibraryOverviewFile libraryOverviewFile = new LibraryOverviewFile("outputPath", "test.md");

    assertTrue("fileName should be test.md", libraryOverviewFile.getFileName() == "test.md");
  }
}
