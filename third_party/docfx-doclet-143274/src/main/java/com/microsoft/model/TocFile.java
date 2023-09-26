package com.microsoft.model;

import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TocFile extends ArrayList<TocItem> implements YmlFile {

  private static final String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";
  private static final String TOC_FILE_NAME = "toc.yml";
  private final String outputPath;
  private final String projectName;
  private final boolean disableChangelog;

  private final boolean disableLibraryOverview;

  public TocFile(
      String outputPath,
      String projectName,
      boolean disableChangelog,
      boolean disableLibraryOverview) {
    this.outputPath = outputPath;
    this.projectName = projectName;
    this.disableChangelog = disableChangelog;
    this.disableLibraryOverview = disableLibraryOverview;
  }

  public void addTocItem(TocItem packageTocItem) {
    add(packageTocItem);
  }

  protected void sortByUid() {
    Collections.sort(this, (a, b) -> a.getUid().compareToIgnoreCase(b.getUid()));
  }

  @Override
  public String getFileContent() {
    sortByUid();
    List<Object> tocContents =
        new TocContents(projectName, disableChangelog, disableLibraryOverview, this).getContents();
    return TOC_FILE_HEADER + YamlUtil.objectToYamlString(tocContents);
  }

  @Override
  public String getFileNameWithPath() {
    return outputPath + File.separator + TOC_FILE_NAME;
  }
}
