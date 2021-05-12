package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocContents {
  private final String projectName;
  private final List<Object> contents = new ArrayList<>();

  public TocContents(String projectName, List<TocItem> items) {
    this.projectName = projectName;

    if (projectName == null || projectName.equals("")) {
      //  don't include product hierarchy or guides if no projectName included
      contents.addAll(items);
    } else {
      createTocContents(projectName, items);
    }
  }

  private void createTocContents(String projectName, List<TocItem> items) {
    List<Object> tocItems = new ArrayList<>();
    // combine guides and tocItems
    tocItems.add(new Guide("Overview", "index.md"));
    tocItems.add(new Guide("Version history", "history.md"));
    tocItems.addAll(items);
    // combine product hierarchy and tocItems
    contents.add(new ProjectContents(projectName, tocItems));
  }

  public List<Object> getContents() {
    return contents;
  }

  public String getProjectName() {
    return projectName;
  }
}
