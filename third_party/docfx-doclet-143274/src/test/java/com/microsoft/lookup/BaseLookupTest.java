package com.microsoft.lookup;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.*;
import com.sun.source.doctree.*;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private DocletEnvironment environment;
    private DocTrees docTrees;
    private DocCommentTree docCommentTree;
    private TextTree textTree;
    private LinkTree linkTree;
    private ReferenceTree referenceTree;
    private LiteralTree literalTree;
    private TypeElement typeElement;
    private BaseLookup<Element> baseLookup;
    private ExtendedMetadataFileItem lastBuiltItem;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        docCommentTree = Mockito.mock(DocCommentTree.class);
        textTree = Mockito.mock(TextTree.class);
        linkTree = Mockito.mock(LinkTree.class);
        referenceTree = Mockito.mock(ReferenceTree.class);
        literalTree = Mockito.mock(LiteralTree.class);
        typeElement = Mockito.mock(TypeElement.class);

        baseLookup = new BaseLookup<>(environment) {
            @Override
            protected ExtendedMetadataFileItem buildMetadataFileItem(Element element) {
                lastBuiltItem = buildExtendedMetadataFileItem(element);
                return lastBuiltItem;
            }
        };
    }

    @Test
    public void determineComment() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(element)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(textTree, linkTree)).when(docCommentTree).getFullBody();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(linkTree.getKind()).thenReturn(Kind.LINK);
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(textTree.toString()).thenReturn("Some text 1");

        String result = baseLookup.determineComment(element);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(element);
        verify(docCommentTree).getFullBody();
        verify(textTree).getKind();
        verify(linkTree).getKind();
        verify(linkTree).getReference();
        verify(linkTree).getLabel();
        assertEquals("Wrong result", result, "Some text 1<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">Some#signature</xref>");
    }

    @Test
    public void makeTypeShort() {
        assertEquals("Wrong result for primitive type", baseLookup.makeTypeShort("int"), "int");
        assertEquals("Wrong result", baseLookup.makeTypeShort("java.lang.String"), "String");
        assertEquals("Wrong result for inner class",
                baseLookup.makeTypeShort("org.apache.commons.lang3.arch.Processor.Arch"), "Processor.Arch");
        assertEquals("Wrong result for class with generic",
                baseLookup.makeTypeShort("java.util.List<java.lang.String>"), "List<String>");
        assertEquals("Wrong result for inner class with generic",
                baseLookup.makeTypeShort("java.util.List.Custom<java.lang.Some.String>"), "List.Custom<Some.String>");
        assertEquals("Wrong result for inner class with complex generic",
                baseLookup.makeTypeShort("a.b.c.D.E.G<m.n.A.B<c.d.D.G<a.F.Z>>>"), "D.E.G<A.B<D.G<F.Z>>>");
        assertEquals("Wrong result for inner class with generic & inheritance",
                baseLookup.makeTypeShort("a.b.G<? extends a.b.List>"), "G<? extends List>");
    }

    @Test
    public void buildXrefTag() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(linkTree.getLabel()).thenReturn(Collections.emptyList());

        String result = baseLookup.buildXrefTag(linkTree);

        assertEquals("Wrong result", result,
                "<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">Some#signature</xref>");
    }

    @Test
    public void buildXrefTagWhenLabelPresents() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        doReturn(Arrays.asList(textTree)).when(linkTree).getLabel();
        String labelValue = "IamLabel";
        when(textTree.toString()).thenReturn(labelValue);

        String result = baseLookup.buildXrefTag(linkTree);

        assertEquals("Wrong result", result,
                "<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">" + labelValue + "</xref>");
    }

    @Test
    public void buildCodeTag() {
        String tagContent = "Some text ≤";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(tagContent);

        String result = baseLookup.buildCodeTag(literalTree);

        assertEquals("Wrong result", result, "<code>" + tagContent + "</code>");
    }

    @Test
    public void expandLiteralBody() {
        String tagContent = "Some text ≤ \u2264";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(tagContent);

        String result = baseLookup.expandLiteralBody(literalTree);
        String expected = "Some text ≤ ≤";

        assertEquals("Wrong result", result, expected);
    }

    @Test
    public void replaceLinksAndCodes() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(linkTree.getLabel()).thenReturn(Collections.emptyList());
        String textTreeContent = "Some text content ≤ \u2264";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(textTreeContent);
        when(linkTree.getKind()).thenReturn(Kind.LINK);
        when(literalTree.getKind()).thenReturn(Kind.CODE);
        when(textTree.getKind()).thenReturn(Kind.TEXT);

        String result = baseLookup.replaceLinksAndCodes(Arrays.asList(linkTree, literalTree, textTree));

        assertEquals("Wrong result", result, "<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">"
                + "Some#signature</xref><code>Some text content ≤ ≤</code>" + textTreeContent);
    }

    @Test
    public void resolve() {
        TypeElement element1 = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        TypeElement element2 = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        ExtendedMetadataFileItem resultForKey1 = baseLookup.resolve(element1);
        ExtendedMetadataFileItem resultForKey2 = baseLookup.resolve(element2);
        ExtendedMetadataFileItem consequenceCallResultForKey1 = baseLookup.resolve(element1);

        assertEquals("Consequence call should return same instance", resultForKey1, consequenceCallResultForKey1);
        assertNotEquals("Resolve for another key should return another instance", resultForKey2, resultForKey1);
    }

    @Test
    public void testExtractMethods() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        assertEquals("Wrong packageName", baseLookup.extractPackageName(element), lastBuiltItem.getPackageName());
        assertEquals("Wrong fullName", baseLookup.extractFullName(element), lastBuiltItem.getFullName());
        assertEquals("Wrong name", baseLookup.extractName(element), lastBuiltItem.getName());
        assertEquals("Wrong href", baseLookup.extractHref(element), lastBuiltItem.getHref());
        assertEquals("Wrong parent", baseLookup.extractParent(element), lastBuiltItem.getParent());
        assertEquals("Wrong id", baseLookup.extractId(element), lastBuiltItem.getId());
        assertEquals("Wrong uid", baseLookup.extractUid(element), lastBuiltItem.getUid());
        assertEquals("Wrong nameWithType", baseLookup.extractNameWithType(element), lastBuiltItem.getNameWithType());
        assertEquals("Wrong methodContent", baseLookup.extractMethodContent(element),
                lastBuiltItem.getMethodContent());
        assertEquals("Wrong fieldContent", baseLookup.extractFieldContent(element), lastBuiltItem.getFieldContent());
        assertEquals("Wrong constructorContent", baseLookup.extractConstructorContent(element),
                lastBuiltItem.getConstructorContent());
        assertEquals("Wrong overload", baseLookup.extractOverload(element), lastBuiltItem.getOverload());
        assertEquals("Wrong parameters", baseLookup.extractParameters(element), lastBuiltItem.getParameters());
        assertEquals("Wrong exceptions", baseLookup.extractExceptions(element), lastBuiltItem.getExceptions());

        assertEquals("Wrong return", baseLookup.extractReturn(element).getReturnType(),
                lastBuiltItem.getReturn().getReturnType());
        assertEquals("Wrong return", baseLookup.extractReturn(element).getReturnDescription(),
                lastBuiltItem.getReturn().getReturnDescription());

        assertEquals("Wrong summary", baseLookup.extractSummary(element), lastBuiltItem.getSummary());
        assertEquals("Wrong type", baseLookup.extractType(element), lastBuiltItem.getType());
        assertEquals("Wrong content", baseLookup.extractContent(element), lastBuiltItem.getContent());
        assertEquals("Wrong typeParameters", baseLookup.extractTypeParameters(element),
                lastBuiltItem.getTypeParameters());
        assertEquals("Wrong superclass", baseLookup.extractSuperclass(element), lastBuiltItem.getSuperclass());
        assertEquals("Wrong interfaces", baseLookup.extractInterfaces(element), lastBuiltItem.getInterfaces());
        assertEquals("Wrong tocName", baseLookup.extractTocName(element), lastBuiltItem.getTocName());
        assertEquals("Wrong references", baseLookup.extractReferences(element), lastBuiltItem.getReferences());
    }

    private ExtendedMetadataFileItem buildExtendedMetadataFileItem(Element element) {
        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(String.valueOf(element));
        result.setPackageName("Some package name");
        result.setFullName("Some full name");
        result.setName("Some name");
        result.setHref("Some href");
        result.setParent("Some parent");
        result.setId("Some id");
        result.setNameWithType("Some name with type");
        result.setMethodContent("Some method content");
        result.setFieldContent("Some field content");
        result.setConstructorContent("Some constructor content");
        result.setOverload("Some overload");
        result.setParameters(Arrays.asList(new MethodParameter("method id", "method type", "method desc")));
        result.setExceptions(Arrays.asList(new ExceptionItem("ex type", "ex desc")));
        result.setReturn(new Return("return type", "return desc"));
        result.setSummary("Some summary");
        result.setType("Some type");
        result.setJavaType("Some type");
        result.setContent("Some content");
        result.setTypeParameters(Arrays.asList(new TypeParameter("type param id")));
        result.setSuperclass(Arrays.asList("Some "));
        result.setInterfaces(Arrays.asList("Some interface"));
        result.setTocName("Some toc name");
        result.addReferences(Set.of(new MetadataFileItem("ref uid")));
        return result;
    }

    @Test
    public void testExtractJavaType() {
        assertEquals("Wrong javaType", baseLookup.extractJavaType(typeElement), null);
    }
}
