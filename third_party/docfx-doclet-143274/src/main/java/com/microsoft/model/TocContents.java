/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocContents {
  private final String projectName;
  private final List<Object> contents = new ArrayList<>();

  public TocContents(
      String projectName,
      boolean disableChangelog,
      boolean disableLibraryOverview,
      List<TocItem> items) {
    this.projectName = projectName;

    if (projectName == null || projectName.equals("")) {
      contents.addAll(items);
    } else {
      //  only include product hierarchy and guides if projectName included
      createTocContents(projectName, disableChangelog, disableLibraryOverview, items);
    }
  }

  private void createTocContents(
      String projectName,
      boolean disableChangelog,
      boolean disableLibraryOverview,
      List<TocItem> items) {
    List<Object> tocItems = new ArrayList<>();
    // combine guides and tocItems
    // If disableLibraryOverview is enabled, then generate old overview. Otherwise generate the new library overview.
    if (!disableLibraryOverview) {
      tocItems.add(new Guide("Overview", "overview.md"));
    }
    else{
      tocItems.add(new Guide("Overview", "overview.html"));
    }
    if (!disableChangelog) {
      tocItems.add(new Guide("Version history", "history.md"));
    }
    tocItems.addAll(items);
    // wrap guides + tocItems with product hierarchy
    contents.add(new ProjectContents(projectName, tocItems));
  }

  public List<Object> getContents() {
    return contents;
  }

  public String getProjectName() {
    return projectName;
  }
}
