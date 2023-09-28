/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;

public class TocTypeMap extends HashMap<String, ArrayList<TocItem>> {

  public TocTypeMap() {
    this.put(ElementKind.CLASS.name(), new ArrayList<>());
    this.put(ElementKind.INTERFACE.name(), new ArrayList<>());
    this.put(ElementKind.ENUM.name(), new ArrayList<>());
    this.put(ElementKind.ANNOTATION_TYPE.name(), new ArrayList<>());
    this.put("EXCEPTION", new ArrayList<>());
  }

  public List<KindTitle> getTitleList() {
    return List.of(
        new KindTitle(ElementKind.INTERFACE.name(), "Interfaces"),
        new KindTitle(ElementKind.CLASS.name(), "Classes"),
        new KindTitle(ElementKind.ENUM.name(), "Enums"),
        new KindTitle(ElementKind.ANNOTATION_TYPE.name(), "Annotation Types"),
        new KindTitle("EXCEPTION", "Exceptions"));
  }

  public List<TocItem> toList() {
    return getTitleList().stream()
        .filter(kindTitle -> get(kindTitle.getElementKind()).size() > 0)
        .flatMap(
            kindTitle -> {
              get(kindTitle.getElementKind()).add(0, new TocItem(kindTitle.getTitle()));
              return get(kindTitle.getElementKind()).stream();
            })
        .collect(Collectors.toList());
  }
}
