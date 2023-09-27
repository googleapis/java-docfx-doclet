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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class TocFileTest {

  @Test
  public void sortsByUid() {
    TocFile tocFile = new TocFile("outputPath", "google-cloud-project", false, false);
    TocItem tocItemA = new TocItem("a.uid.package.class", "name");
    TocItem tocItemB = new TocItem("B.uid.package.class", "name");
    TocItem tocItemC = new TocItem("c.uid.package.class", "name");
    TocItem olderItem = new TocItem("Older and prerelease packages", "name");

    tocFile.addTocItem(tocItemC);
    tocFile.addTocItem(tocItemA);
    tocFile.addTocItem(olderItem);
    tocFile.addTocItem(tocItemB);

    assertThat(tocFile).containsExactly(tocItemC, tocItemA, olderItem, tocItemB).inOrder();

    tocFile.sortByUid();

    assertThat(tocFile).containsExactly(tocItemA, tocItemB, tocItemC, olderItem).inOrder();
  }
}
