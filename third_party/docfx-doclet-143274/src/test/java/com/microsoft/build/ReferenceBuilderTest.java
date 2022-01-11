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

import com.microsoft.lookup.ClassLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.util.ElementUtil;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReferenceBuilderTest {

    private ReferenceBuilder referenceBuilder;

    @Before
    public void setup() {
        DocletEnvironment environment = Mockito.mock(DocletEnvironment.class);
        ElementUtil elementUtil = new ElementUtil(new String[0], new String[0]);
        ClassLookup classLookup = new ClassLookup(environment);
        referenceBuilder = new ReferenceBuilder(environment, classLookup, elementUtil);
    }

    @Test
    public void expandComplexGenericsInReferences() {
        MetadataFile classMetadataFile = new MetadataFile("path", "name");
        MetadataFileItem referenceItem = new MetadataFileItem("a.b.c.List<df.mn.ClassOne<tr.T>>");
        Set<MetadataFileItem> references = classMetadataFile.getReferences();
        references.add(referenceItem);

        referenceBuilder.expandComplexGenericsInReferences(classMetadataFile);

        assertEquals("Wrong references amount", references.size(), 4);

        List<String> content = references.stream().map(MetadataFileItem::getUid).collect(Collectors.toList());
        assertTrue("Wrong references content", content.contains("a.b.c.List"));
        assertTrue("Wrong references content", content.contains("df.mn.ClassOne"));
        assertTrue("Wrong references content", content.contains("tr.T"));
        assertTrue("Wrong references content", content.contains("a.b.c.List<df.mn.ClassOne<tr.T>>"));
    }

    //todo add test case to cover reference item with in package
    @Test
    public void buildRefItem() {
        buildRefItemAndCheckAssertions("java.lang.Some.String", "java.lang.Some.String", "String");
        buildRefItemAndCheckAssertions("java.lang.Some.String[]", "java.lang.Some.String[]", "String");
    }

    private void buildRefItemAndCheckAssertions(String initialValue, String expectedUid, String expectedName) {
        MetadataFileItem result = referenceBuilder.buildRefItem(initialValue);

        assertEquals("Wrong uid", result.getUid(), expectedUid);
        assertEquals("Wrong name", result.getSpecForJava().iterator().next().getUid(), RegExUtils.removeAll(expectedUid, "\\[\\]$"));
        assertEquals("Wrong name", result.getSpecForJava().iterator().next().getName(), expectedName);
        assertEquals("Wrong fullName", result.getSpecForJava().iterator().next().getFullName(), RegExUtils.removeAll(expectedUid, "\\[\\]$"));
    }

    @Test
    public void getJavaReferenceHref() {
        String result1 = referenceBuilder.getJavaReferenceHref("java.lang.Object");
        String result2 = referenceBuilder.getJavaReferenceHref("java.lang.Object.equals(java.lang.Object)");
        String result3 = referenceBuilder.getJavaReferenceHref("java.lang.Object.notify()");
        String result4 = referenceBuilder.getJavaReferenceHref("java.util.List");
        String result5 = referenceBuilder.getJavaReferenceHref("java.lang.Object.wait(long,int)");
        String result6 = referenceBuilder.getJavaReferenceHref("java.lang.Object.getClass()");
        String result7 = referenceBuilder.getJavaReferenceHref("java.io.IOException");
        String result8 = referenceBuilder.getJavaReferenceHref("java.io.InputStream");
        String result9 = referenceBuilder.getJavaReferenceHref("java.lang.Enum.hashCode()");
        String result10 = referenceBuilder.getJavaReferenceHref("java.nio.ByteBuffer");
        String result11 = referenceBuilder.getJavaReferenceHref("java.lang.Enum.<T>valueOf(java.lang.Class<T>,java.lang.String)");
        String result12 = referenceBuilder.getJavaReferenceHref("");
        String result13 = referenceBuilder.getJavaReferenceHref(null);

        String baseURL = "https://docs.oracle.com/javase/8/docs/api/";

        assertEquals(baseURL + "java/lang/Object.html", result1);
        assertEquals(baseURL + "java/lang/Object.html#equals-java.lang.Object-", result2);
        assertEquals(baseURL + "java/lang/Object.html#notify--", result3);
        assertEquals(baseURL + "java/util/List.html", result4);
        assertEquals(baseURL + "java/lang/Object.html#wait-long-int-", result5);
        assertEquals(baseURL + "java/lang/Object.html#getClass--", result6);
        assertEquals(baseURL + "java/io/IOException.html", result7);
        assertEquals(baseURL + "java/io/InputStream.html", result8);
        assertEquals(baseURL + "java/lang/Enum.html#hashCode--", result9);
        assertEquals(baseURL + "java/nio/ByteBuffer.html", result10);
        assertEquals(baseURL + "java/lang/Enum.html#valueOf-java.lang.Class-java.lang.String-", result11);
        assertEquals(baseURL, result12);
        assertEquals(baseURL, result13);
    }
}
