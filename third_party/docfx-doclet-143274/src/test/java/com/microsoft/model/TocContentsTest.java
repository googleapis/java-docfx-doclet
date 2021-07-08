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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TocContentsTest {

    private TocItem tocItemA;
    private TocItem tocItemB;
    private TocItem tocItemC;
    private List<TocItem> tocItems;
    private String projectName = "google-cloud-project";

    @Before
    public void setup(){
        tocItemA = new TocItem("A.uid.package.class", "nameA");
        tocItemB = new TocItem("B.uid.package.class", "nameB");
        tocItemC = new TocItem("C.uid.package.class", "nameC");

        tocItems = new ArrayList<>();
        tocItems.add(tocItemA);
        tocItems.add(tocItemB);
        tocItems.add(tocItemC);
    }

    @Test
    public void getContentsWithProjectName() {
        //  should include ProjectContents and Guides
        List<Object> tocContents = new TocContents(projectName, tocItems).getContents();

        assertEquals("Should only include 1 item", tocContents.size(), 1);
        assertEquals("Should include ProjectContents", tocContents.get(0).getClass(), ProjectContents.class);

        ProjectContents contents = (ProjectContents) tocContents.get(0);
        assertEquals(contents.getName(),"google-cloud-project");

        List<Object> items = contents.getItems();
        assertEquals("Should be 5 items", items.size(), 5);

        assertEquals("Guide should be first", items.get(0).getClass(), Guide.class);
        Guide overview = (Guide) items.get(0);
        assertEquals("First guide should be Overview", overview.getName(), "Overview");
        assertEquals("Guide should be second", items.get(1).getClass(), Guide.class);
        Guide history = (Guide) items.get(1);
        assertEquals("Second guide should be Version History", history.getName(), "Version history");

        assertEquals("Item A should be second", items.get(2), tocItemA);
        assertEquals("Item B should be third", items.get(3), tocItemB);
        assertEquals("Item C should be fourth", items.get(4), tocItemC);
    }

    @Test
    public void getContentsNoProjectName() {
        List<Object> tocContents = new TocContents("", tocItems).getContents();

        //  should not include ProjectContents or Guides
        assertEquals("Should be 3 items", tocContents.size(), 3);
        assertEquals("Item A should be first", tocContents.get(0), tocItemA);
        assertEquals("Item B should be second", tocContents.get(1), tocItemB);
        assertEquals("Item C should be third", tocContents.get(2), tocItemC);
    }
}