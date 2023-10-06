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
package com.microsoft.build;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.util.ElementUtil;
import com.sun.source.util.DocTrees;
import java.io.File;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class ClassBuilderTest {
  @Rule public CompilationRule rule = new CompilationRule();
  private Elements elements;
  private ClassBuilder classBuilder;
  private DocletEnvironment environment;
  private DocTrees docTrees;

  @Before
  public void setup() {
    elements = rule.getElements();
    environment = Mockito.mock(DocletEnvironment.class);
    docTrees = Mockito.mock(DocTrees.class);
    ElementUtil elementUtil = new ElementUtil(new String[0], new String[0]);
    ClassLookup classLookup = new ClassLookup(environment, elementUtil);
    PackageLookup packageLookup = new PackageLookup(environment);
    classBuilder =
        new ClassBuilder(
            elementUtil,
            classLookup,
            new ClassItemsLookup(environment, elementUtil),
            "./target",
            packageLookup,
            new ReferenceBuilder(environment, classLookup, elementUtil));
  }

  @Test
  public void addConstructorsInfoWhenOnlyDefaultConstructor() {
    TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
    MetadataFile container = new MetadataFile("output", "name");
    when(environment.getElementUtils()).thenReturn(elements);
    when(environment.getDocTrees()).thenReturn(docTrees);

    classBuilder.addConstructorsInfo(element, container);

    assertEquals(
        "Wrong file name", container.getFileNameWithPath(), "output" + File.separator + "name");
    assertEquals("Container should contain constructor item", container.getItems().size(), 1);
  }

  @Test
  public void addConstructorsInfo() {
    TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
    MetadataFile container = new MetadataFile("output", "name");
    when(environment.getElementUtils()).thenReturn(elements);
    when(environment.getDocTrees()).thenReturn(docTrees);

    classBuilder.addConstructorsInfo(element, container);

    assertEquals(
        "Wrong file name", container.getFileNameWithPath(), "output" + File.separator + "name");
    Collection<MetadataFileItem> constructorItems = container.getItems();
    assertEquals("Container should contain 2 constructor items", constructorItems.size(), 2);
  }
}
