package com.microsoft.build;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Syntax;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class YmlFilesBuilderTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private YmlFilesBuilder ymlFilesBuilder;
    private DocletEnvironment environment;
    private DocTrees docTrees;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        ymlFilesBuilder = new YmlFilesBuilder(environment, "./target", new String[]{}, new String[]{}, "google-cloud-product");
    }

    @Test
    public void addConstructorsInfoWhenOnlyDefaultConstructor() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        MetadataFile container = new MetadataFile("output", "name");
        when(environment.getElementUtils()).thenReturn(elements);
        when(environment.getDocTrees()).thenReturn(docTrees);

        ymlFilesBuilder.addConstructorsInfo(element, container);

        assertEquals("Wrong file name", container.getFileNameWithPath(), "output" + File.separator + "name");
        assertEquals("Container should contain constructor item", container.getItems().size(),1);
    }

    @Test
    public void addConstructorsInfo() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        MetadataFile container = new MetadataFile("output", "name");
        when(environment.getElementUtils()).thenReturn(elements);
        when(environment.getDocTrees()).thenReturn(docTrees);

        ymlFilesBuilder.addConstructorsInfo(element, container);

        assertEquals("Wrong file name", container.getFileNameWithPath(), "output" + File.separator + "name");
        Collection<MetadataFileItem> constructorItems = container.getItems();
        assertEquals("Container should contain 2 constructor items", constructorItems.size(), 2);
    }

    //todo add test case to cover reference item with in package
    @Test
    public void buildRefItem() {
        buildRefItemAndCheckAssertions("java.lang.Some.String", "java.lang.Some.String", "String");
        buildRefItemAndCheckAssertions("java.lang.Some.String[]", "java.lang.Some.String[]", "String");
    }

    private void buildRefItemAndCheckAssertions(String initialValue, String expectedUid, String expectedName) {
        MetadataFileItem result = ymlFilesBuilder.buildRefItem(initialValue);

        assertEquals("Wrong uid", result.getUid(), expectedUid);
        assertEquals("Wrong name", result.getSpecForJava().iterator().next().getUid(), RegExUtils.removeAll(expectedUid, "\\[\\]$"));
        assertEquals("Wrong name", result.getSpecForJava().iterator().next().getName(), expectedName);
        assertEquals("Wrong fullName", result.getSpecForJava().iterator().next().getFullName(), RegExUtils.removeAll(expectedUid, "\\[\\]$"));
    }

    @Test
    public void populateUidValues() {
        MetadataFile classMetadataFile = new MetadataFile("output", "name");

        MetadataFileItem ownerClassItem = buildMetadataFileItem("a.b.OwnerClass", "Not important summary value");
        ownerClassItem.setNameWithType("OwnerClass");
        MetadataFileItem item1 = buildMetadataFileItem("UID unknown class", "UnknownClass");
        populateSyntax(item1, "SomeClass#someMethod(String param)");
        MetadataFileItem item2 = buildMetadataFileItem("UID known class", "SomeClass#someMethod(String param)");
        MetadataFileItem item3 = buildMetadataFileItem("UID method only", "#someMethod2(String p1, String p2)");
        classMetadataFile.getItems().addAll(Arrays.asList(ownerClassItem, item1, item2, item3));

        MetadataFileItem reference1 = new MetadataFileItem("a.b.SomeClass.someMethod(String param)");
        reference1.setNameWithType("SomeClass.someMethod(String param)");
        MetadataFileItem reference2 = new MetadataFileItem("a.b.OwnerClass.someMethod2(String p1, String p2)");
        reference2.setNameWithType("OwnerClass.someMethod2(String p1, String p2)");
        classMetadataFile.getReferences().addAll(Arrays.asList(reference1, reference2));

        ymlFilesBuilder.populateUidValues(Collections.emptyList(), Arrays.asList(classMetadataFile));

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
    public void determineUidByLinkContent() {
        Map<String, String> lookup = new HashMap<>() {{
            put("SomeClass", "a.b.c.SomeClass");
            put("SomeClass.someMethod()", "a.b.c.SomeClass.someMethod()");
            put("SomeClass.someMethod(String param)", "a.b.c.SomeClass.someMethod(String param)");
        }};

        LookupContext lookupContext = new LookupContext(lookup, lookup);
        assertEquals("Wrong result for class", ymlFilesBuilder.
                resolveUidByLookup("SomeClass", lookupContext),"a.b.c.SomeClass");
        assertEquals("Wrong result for method", ymlFilesBuilder.
                resolveUidFromLinkContent("SomeClass#someMethod()", lookupContext),"a.b.c.SomeClass.someMethod()");
        assertEquals("Wrong result for method with param", ymlFilesBuilder.
                        resolveUidFromLinkContent("SomeClass#someMethod(String param)", lookupContext),
                  "a.b.c.SomeClass.someMethod(String param)");

        assertEquals("Wrong result for unknown class", ymlFilesBuilder.
                  resolveUidByLookup("UnknownClass", lookupContext),"");
        assertEquals("Wrong result for null", ymlFilesBuilder.resolveUidByLookup(null, lookupContext), "");
        assertEquals("Wrong result for whitespace", ymlFilesBuilder.resolveUidByLookup(" ", lookupContext), "");
    }

    @Test
    public void splitUidWithGenericsIntoClassNames() {
        List<String> result = ymlFilesBuilder.splitUidWithGenericsIntoClassNames("a.b.c.List<df.mn.ClassOne<tr.T>>");

        assertEquals("Wrong result list size", result.size(), 3);
        assertTrue("Wrong result list content", result.contains("a.b.c.List"));
        assertTrue("Wrong result list content", result.contains("df.mn.ClassOne"));
        assertTrue("Wrong result list content", result.contains("tr.T"));
    }

    @Test
    public void expandComplexGenericsInReferences() {
        MetadataFile classMetadataFile = new MetadataFile("path", "name");
        MetadataFileItem referenceItem = new MetadataFileItem("a.b.c.List<df.mn.ClassOne<tr.T>>");
        Set<MetadataFileItem> references = classMetadataFile.getReferences();
        references.add(referenceItem);

        ymlFilesBuilder.expandComplexGenericsInReferences(classMetadataFile);

        assertEquals("Wrong references amount", references.size(),4);

        List<String> content = references.stream().map(MetadataFileItem::getUid).collect(Collectors.toList());
        assertTrue("Wrong references content", content.contains("a.b.c.List"));
        assertTrue("Wrong references content", content.contains("df.mn.ClassOne"));
        assertTrue("Wrong references content", content.contains("tr.T"));
        assertTrue("Wrong references content", content.contains("a.b.c.List<df.mn.ClassOne<tr.T>>"));
    }
}
