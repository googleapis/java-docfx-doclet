package com.microsoft.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LibraryOverviewFileTest {

  @Test
  public void testEquals() {
    LibraryOverviewFile libraryOverviewFile = new LibraryOverviewFile("outputPath", "test.md");

    assertTrue("fileName should be test.md", libraryOverviewFile.getFileName() == "test.md");
  }
}
