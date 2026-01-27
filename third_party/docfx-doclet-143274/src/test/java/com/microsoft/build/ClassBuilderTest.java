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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.docfx.doclet.RepoMetadata;
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
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Name; // Required for mocking getSimpleName()
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

  @Test
  public void createClientOverviewTable_usesLibraryPathOverride() {
    // 1. Setup Mock RepoMetadata
    RepoMetadata repoMetadata = new RepoMetadata();
    repoMetadata.setRepo("googleapis/java-firestore");
    repoMetadata.setDistributionName("com.google.cloud:google-cloud-firestore:1.0.0");

    Map<String, String> overrides = new HashMap<>();
    overrides.put("FirestoreAdminClient", "google-cloud-firestore-admin");
    repoMetadata.setLibraryPathOverrides(overrides);

    // 2. Mock ClassLookup and Element
    ClassLookup classLookup = Mockito.mock(ClassLookup.class);
    TypeElement classElement = Mockito.mock(TypeElement.class);

    Name simpleName = Mockito.mock(Name.class);
    when(simpleName.toString()).thenReturn("FirestoreAdminClient");
    when(classElement.getSimpleName()).thenReturn(simpleName);

    when(classLookup.extractUid(classElement))
        .thenReturn("com.google.cloud.firestore.v1.FirestoreAdminClient");

    // 3. Test
    ClassBuilder builder = new ClassBuilder(null, classLookup, null, null, null, null);

    try {
      java.lang.reflect.Method method =
          ClassBuilder.class.getDeclaredMethod(
              "createClientOverviewTable", TypeElement.class, RepoMetadata.class);
      method.setAccessible(true);
      String html = (String) method.invoke(builder, classElement, repoMetadata);

      // 4. Verify link contains "google-cloud-firestore-admin" and the double slash "//"
      assertTrue(
          "Link should use the override directory",
          html.contains(
              "googleapis/java-firestore/tree/main//google-cloud-firestore-admin/src/main/java"));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
