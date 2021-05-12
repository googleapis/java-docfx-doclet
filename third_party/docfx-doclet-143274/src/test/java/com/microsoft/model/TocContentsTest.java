package com.microsoft.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TocContentsTest {

  @Test
  public void getContentsWithProjectName() {
    String projectName = "google-cloud-project";
    List<TocItem> tocItems = new ArrayList<>();

    TocItem tocItemA = new TocItem("A.uid.package.class", "name");
    TocItem tocItemB = new TocItem("B.uid.package.class", "name");
    TocItem tocItemC = new TocItem("C.uid.package.class", "name");

    tocItems.add(tocItemA);
    tocItems.add(tocItemB);
    tocItems.add(tocItemC);

    //  should include ProjectContents and Guides
    List<Object> tocContents = new TocContents(projectName, tocItems).getContents();
    assertThat("Should only include 1 item", tocContents.size(), is(1));
    assertThat(
        "Should include ProjectContents", tocContents.get(0), instanceOf(ProjectContents.class));

    ProjectContents contents = (ProjectContents) tocContents.get(0);
    assertThat(contents.getName(), is("google-cloud-project"));

    List<Object> items = contents.getItems();
    assertThat("Should be 5 items", items.size(), is(5));

    assertThat("Guide should be first", items.get(0), instanceOf(Guide.class));
    Guide overview = (Guide) items.get(0);
    assertThat("First guide should be Overview", overview.getName(), is("Overview"));
    assertThat("Guide should be second", items.get(1), instanceOf(Guide.class));
    Guide history = (Guide) items.get(1);
    assertThat("Second guide should be Version History", history.getName(), is("Version history"));

    assertThat("Item A should be second", items.get(2), is(tocItemA));
    assertThat("Item B should be third", items.get(3), is(tocItemB));
    assertThat("Item C should be fourth", items.get(4), is(tocItemC));
  }

  @Test
  public void getContentsNoProjectName() {
    List<TocItem> tocItems = new ArrayList<>();

    TocItem tocItemA = new TocItem("A.uid.package.class", "name");
    TocItem tocItemB = new TocItem("B.uid.package.class", "name");
    TocItem tocItemC = new TocItem("C.uid.package.class", "name");

    tocItems.add(tocItemA);
    tocItems.add(tocItemB);
    tocItems.add(tocItemC);

    List<Object> tocContents = new TocContents("", tocItems).getContents();

    //  should not include ProjectContents or Guides
    assertThat("Should be 3 items", tocContents.size(), is(3));
    assertThat("Item A should be first", tocContents.get(0), is(tocItemA));
    assertThat("Item B should be second", tocContents.get(1), is(tocItemB));
    assertThat("Item C should be third", tocContents.get(2), is(tocItemC));
  }
}
