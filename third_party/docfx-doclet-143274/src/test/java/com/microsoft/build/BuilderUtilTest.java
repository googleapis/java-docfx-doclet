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

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Syntax;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BuilderUtilTest {
    @Test
    public void testPopulateUidValues() {
        MetadataFile classMetadataFile = new MetadataFile("output", "name");

        MetadataFileItem ownerClassItem = buildMetadataFileItem("a.b.OwnerClass", "Not important summary value");
        ownerClassItem.setNameWithType("OwnerClass");
        MetadataFileItem item1 = buildMetadataFileItem("UID unknown class", "UnknownClass");
        populateSyntax(item1, "SomeClass#someMethod(String param)");
        MetadataFileItem item2 = buildMetadataFileItem("UID known class", "SomeClass#someMethod(String param)");
        MetadataFileItem item3 = buildMetadataFileItem("UID method only", "#someMethod2(String p1, String p2)");
        item3.setPackageName("a.b");
        classMetadataFile.getItems().addAll(Arrays.asList(ownerClassItem, item1, item2, item3));

        MetadataFileItem reference1 = new MetadataFileItem("a.b.SomeClass.someMethod(String param)");
        reference1.setPackageName("a.b");
        reference1.setNameWithType("SomeClass.someMethod(String param)");
        MetadataFileItem reference2 = new MetadataFileItem("a.b.OwnerClass.someMethod2(String p1, String p2)");
        reference2.setPackageName("a.b");
        reference2.setNameWithType("OwnerClass.someMethod2(String p1, String p2)");
        MetadataFileItem reference3 = new MetadataFileItem("c.d.OwnerClass.someMethod2(String p1, String p2)");
        reference3.setPackageName("c.d");
        reference3.setNameWithType("OwnerClass.someMethod2(String p1, String p2)");
        classMetadataFile.getReferences().addAll(Arrays.asList(reference1, reference2, reference3));

        BuilderUtil.populateUidValues(Collections.emptyList(), List.of(classMetadataFile));

        assertEquals("Wrong summary for unknown class", item1.getSummary(),
                "Bla bla <xref uid=\"\" data-throw-if-not-resolved=\"false\">UnknownClass</xref> bla");
        assertEquals("Wrong syntax description", item1.getSyntax().getParameters().get(0).getDescription(),
                "One two <xref uid=\"a.b.SomeClass.someMethod(String param)\" data-throw-if-not-resolved=\"false\">SomeClass#someMethod(String param)</xref> three");
        assertEquals("Wrong summary for known class", item2.getSummary(),
                "Bla bla <xref uid=\"a.b.SomeClass.someMethod(String param)\" data-throw-if-not-resolved=\"false\">SomeClass#someMethod(String param)</xref> bla");
        assertEquals("Wrong summary for method", item3.getSummary(),
                "Bla bla <xref uid=\"a.b.OwnerClass.someMethod2(String p1, String p2)\" data-throw-if-not-resolved=\"false\">#someMethod2(String p1, String p2)</xref> bla");

    }

    private MetadataFileItem buildMetadataFileItem(String uid, String value) {
        MetadataFileItem item = new MetadataFileItem(uid);
        item.setSummary(
                String.format("Bla bla <xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref> bla", value, value));
        return item;
    }

    private void populateSyntax(MetadataFileItem item, String value) {
        Syntax syntax = new Syntax();
        String methodParamDescription = String
                .format("One two <xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref> three", value, value);
        syntax.setParameters(
                Arrays.asList(new MethodParameter("method param id", "method param type", methodParamDescription)));
        item.setSyntax(syntax);
    }

    @Test
    public void testDetermineUidByLinkContent() {
        // Map similar to what is created in Lookup#consume()
        Map<String, String> lookup = new HashMap<>() {{
            put("SomeClass", "a.b.c.SomeClass");
            put("SomeClass.someMethod()", "a.b.c.SomeClass.someMethod()");
            put("SomeClass.someMethod(String param)", "a.b.c.SomeClass.someMethod(String param)");
            put("a.b.c.SomeClass", "a.b.c.SomeClass");
            put("a.b.c.SomeClass.someMethod()", "a.b.c.SomeClass.someMethod()");
            put("a.b.c.SomeClass.someMethod(String param)", "a.b.c.SomeClass.someMethod(String param)");
            // Duplicate entry to simulate same ClassName in a different package
            put("SomeClass", "d.e.f.SomeClass");
            put("d.e.f.SomeClass", "d.e.f.SomeClass");
        }};

        String packageName = "a.b.c";
        String otherPackageName = "d.e.f";

        LookupContext lookupContext = new LookupContext(lookup, lookup);
        assertEquals("Wrong result for class",
                BuilderUtil.resolveUidFromLinkContent("SomeClass", packageName, lookupContext), "a.b.c.SomeClass");
        assertEquals("Wrong result for method",
                BuilderUtil.resolveUidFromLinkContent("SomeClass#someMethod()", packageName, lookupContext), "a.b.c.SomeClass.someMethod()");
        assertEquals("Wrong result for method with param",
                BuilderUtil.resolveUidFromLinkContent("SomeClass#someMethod(String param)", packageName, lookupContext),
                "a.b.c.SomeClass.someMethod(String param)");
        assertEquals("Wrong result for class with duplicate className",
                BuilderUtil.resolveUidFromLinkContent("SomeClass", otherPackageName, lookupContext), "d.e.f.SomeClass");

        assertEquals("Wrong result for unknown class", BuilderUtil.
                resolveUidByLookup("UnknownClass", lookupContext), "");
        assertEquals("Wrong result for null", BuilderUtil.resolveUidByLookup(null, lookupContext), "");
        assertEquals("Wrong result for whitespace", BuilderUtil.resolveUidByLookup(" ", lookupContext), "");
    }

    @Test
    public void splitUidWithGenericsIntoClassNames() {
        List<String> result = BuilderUtil.splitUidWithGenericsIntoClassNames("a.b.c.List<df.mn.ClassOne<tr.T>>");

        assertEquals("Wrong result list size", result.size(), 3);
        assertTrue("Wrong result list content", result.contains("a.b.c.List"));
        assertTrue("Wrong result list content", result.contains("df.mn.ClassOne"));
        assertTrue("Wrong result list content", result.contains("tr.T"));
    }
}
