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

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TocTypeMapTest {
  @Test
  public void elementKindsExistInMap() {
    TocTypeMap tocTypeMap = new TocTypeMap();
    List<KindTitle> titleList = tocTypeMap.getTitleList();

    assertEquals("Should include 5 items in list", 5, titleList.size());

    titleList.stream()
        .forEach(
            kindtitle ->
                assertNotNull(
                    "Element kind should exist in map",
                    tocTypeMap.get(kindtitle.getElementKind())));

    assertNull("Should not include provided key", tocTypeMap.get("FAKE_VALUE"));
  }
}
