package com.microsoft.lookup;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClassItemsLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private List<Element> allGenderElements;
    private List<Element> allPersonElements;
    private DocletEnvironment environment;
    private DocTrees docTrees;
    private DocCommentTree docCommentTree;
    private ParamTree paramTree;
    private ThrowsTree throwsTree;
    private ReturnTree returnTree;
    private DeprecatedTree deprecatedTree;
    private TextTree textTree;
    private IdentifierTree identifierTree;
    private ClassItemsLookup classItemsLookup;

    @Before
    public void setup() {
        elements = rule.getElements();
        allGenderElements = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender")
                .getEnclosedElements().stream().collect(Collectors.toList());
        allPersonElements = elements.getTypeElement("com.microsoft.samples.subpackage.Person")
                .getEnclosedElements().stream().collect(Collectors.toList());
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        docCommentTree = Mockito.mock(DocCommentTree.class);
        paramTree = Mockito.mock(ParamTree.class);
        throwsTree = Mockito.mock(ThrowsTree.class);
        returnTree = Mockito.mock(ReturnTree.class);
        deprecatedTree = Mockito.mock(DeprecatedTree.class);
        textTree = Mockito.mock(TextTree.class);
        identifierTree = Mockito.mock(IdentifierTree.class);
        classItemsLookup = new ClassItemsLookup(environment, null);

    }

    @Test
    public void extractParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(paramTree)).when(docCommentTree).getBlockTags();
        when(paramTree.getKind()).thenReturn(Kind.PARAM);
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(paramTree.getName()).thenReturn(identifierTree);
        doReturn(Arrays.asList(textTree)).when(paramTree).getDescription();
        when(textTree.toString()).thenReturn("some text bla", "some text bla-bla");
        when(identifierTree.toString()).thenReturn("incomingDamage", "damageType");

        List<MethodParameter> result = classItemsLookup.extractParameters(method);

        verify(environment, times(2)).getDocTrees();
        verify(docTrees, times(2)).getDocCommentTree(method);
        verify(docCommentTree, times(2)).getBlockTags();
        assertEquals("Wrong parameters count", result.size(), 2);

        assertEquals("Wrong first param id", result.get(0).getId(), "incomingDamage");
        assertEquals("Wrong first param type", result.get(0).getType(), "int");
        assertEquals("Wrong first param description", result.get(0).getDescription(), "some text bla");

        assertEquals("Wrong second param id", result.get(1).getId(), "damageType");
        assertEquals("Wrong second param type", result.get(1).getType(), "java.lang.String");
        assertEquals("Wrong second param description", result.get(1).getDescription(), "some text bla-bla");
    }

    @Test
    public void extractParameterDescription() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);
        String paramName = "incomingDamage";

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(paramTree)).when(docCommentTree).getBlockTags();
        when(paramTree.getKind()).thenReturn(Kind.PARAM);
        doReturn(identifierTree).when(paramTree).getName();
        doReturn(Arrays.asList(textTree)).when(paramTree).getDescription();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(textTree.toString()).thenReturn("some weird text");
        when(identifierTree.toString()).thenReturn(paramName);

        String result = classItemsLookup.extractParameterDescription(method, paramName);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(method);
        verify(docCommentTree).getBlockTags();
        assertEquals("Wrong param description", result, "some weird text");
    }

    @Test
    public void extractExceptions() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(throwsTree)).when(docCommentTree).getBlockTags();
        when(throwsTree.getKind()).thenReturn(Kind.THROWS);
        doReturn(Arrays.asList(textTree)).when(throwsTree).getDescription();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(textTree.toString()).thenReturn("some text");

        List<ExceptionItem> result = classItemsLookup.extractExceptions(method);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(method);
        verify(docCommentTree).getBlockTags();
        verify(throwsTree).getKind();
        assertEquals("Wrong exceptions count", result.size(), 1);
        assertEquals("Wrong type", result.get(0).getType(), "java.lang.IllegalArgumentException");
        assertEquals("Wrong description", result.get(0).getDescription(), "some text");
    }

    @Test
    public void extractExceptionDescription() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(throwsTree)).when(docCommentTree).getBlockTags();
        when(throwsTree.getKind()).thenReturn(Kind.THROWS);
        doReturn(Arrays.asList(textTree)).when(throwsTree).getDescription();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(textTree.toString()).thenReturn("some weird text");

        String result = classItemsLookup.extractExceptionDescription(method);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(method);
        verify(docCommentTree).getBlockTags();
        verify(throwsTree).getKind();
        assertEquals("Wrong description", result, "some weird text");
    }

    @Test
    public void extractReturnForExecutableElement() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method0 = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);
        ExecutableElement method1 = ElementFilter.methodsIn(element.getEnclosedElements()).get(1);
        ExecutableElement method2 = ElementFilter.methodsIn(element.getEnclosedElements()).get(2);

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method0)).thenReturn(docCommentTree);
        when(docTrees.getDocCommentTree(method1)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(returnTree)).when(docCommentTree).getBlockTags();
        when(returnTree.getKind()).thenReturn(Kind.RETURN);
        doReturn(Arrays.asList(textTree)).when(returnTree).getDescription();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(textTree.toString()).thenReturn("bla", "bla-bla");

        checkReturnForExecutableElement(method0, "int", "bla");
        checkReturnForExecutableElement(method1, "java.lang.String", "bla-bla");
        checkVoidReturnForExecutableElement(method2);

        verify(environment, times(2)).getDocTrees();
        verify(docTrees).getDocCommentTree(method0);
        verify(docTrees).getDocCommentTree(method1);
        verify(docCommentTree, times(2)).getBlockTags();
        verify(returnTree, times(2)).getKind();
        verify(textTree, times(2)).getKind();
    }

    private void checkReturnForExecutableElement(ExecutableElement executableElement, String expectedType,
                                                 String expectedDescription) {
        Return result = classItemsLookup.extractReturn(executableElement);

        assertEquals(result.getReturnType(), expectedType);
        assertEquals(result.getReturnDescription(), expectedDescription);
    }

    private void checkVoidReturnForExecutableElement(ExecutableElement executableElement) {
        assertNull(classItemsLookup.extractReturn(executableElement));
    }

    @Test
    public void extractReturnDescription() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExecutableElement method0 = ElementFilter.methodsIn(element.getEnclosedElements()).get(0);

        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(method0)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(returnTree)).when(docCommentTree).getBlockTags();
        when(returnTree.getKind()).thenReturn(Kind.RETURN);
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        doReturn(Arrays.asList(textTree)).when(returnTree).getDescription();
        when(textTree.toString()).thenReturn("bla-bla description");

        String result = classItemsLookup.extractReturnDescription(method0);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(method0);
        verify(docCommentTree).getBlockTags();
        verify(returnTree).getKind();
        assertEquals("Wrong description", result, "bla-bla description");
    }

    @Test
    public void extractReturnForVariableElement() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        checkReturnForVariableElement(element, 0, "java.lang.String");
        checkReturnForVariableElement(element, 1, "java.lang.String");
        checkReturnForVariableElement(element, 2, "int");
        checkReturnForVariableElement(element, 3, "int");
        checkReturnForVariableElement(element, 4, "java.lang.String");
    }

    private void checkReturnForVariableElement(TypeElement element, int variableNumber, String expectedType) {
        VariableElement variableElement = ElementFilter.fieldsIn(element.getEnclosedElements()).get(variableNumber);

        Return result = classItemsLookup.extractReturn(variableElement);

        assertEquals(result.getReturnType(), expectedType);
    }

    @Test
    public void convertFullNameToOverload() {
        assertEquals("Wrong result", classItemsLookup.convertFullNameToOverload(
                "com.microsoft.samples.SuperHero.successfullyAttacked(int,java.lang.String)"),
                "com.microsoft.samples.SuperHero.successfullyAttacked*");

        assertEquals("Wrong result for case with generics", classItemsLookup.convertFullNameToOverload(
                "com.microsoft.samples.subpackage.Display<T,R>.show()"),
                "com.microsoft.samples.subpackage.Display<T,R>.show*");

        assertEquals("Wrong result for constructor case", classItemsLookup.convertFullNameToOverload(
                "com.microsoft.samples.SuperHero.SuperHero()"),
                "com.microsoft.samples.SuperHero.SuperHero*");
    }

    @Test
    public void determineTypeForEnumConstant() {
        TypeElement element = elements
                .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        assertEquals(classItemsLookup.determineType(element.getEnclosedElements().get(0)), "Field");
        assertEquals(classItemsLookup.determineType(element.getEnclosedElements().get(1)), "Field");
    }

    @Test
    public void testExtractJavaTypeStaticMethod() {
        Element staticMethod = getElementByName(allGenderElements,"valueOf(java.lang.String)");
        assertEquals("Wrong javaType","static method", classItemsLookup.extractJavaType(staticMethod));
    }

    @Test
    public void testExtractJavaTypeStaticField() {
        Element field = getElementByName(allGenderElements,"FEMALE");
        assertEquals("Wrong javaType", "static field", classItemsLookup.extractJavaType(field));
    }

    @Test
    public void testExtractJavaTypeNonStatic() {
        Element constructor = getElementByName(allGenderElements,"Gender()");
        assertEquals("Wrong javaType",null, classItemsLookup.extractJavaType(constructor));

        Element nonStaticMethod = getElementByName(allPersonElements,"getFirstName()");
        assertEquals("Wrong javaType",null, classItemsLookup.extractJavaType(nonStaticMethod));

        Element nonStaticField = getElementByName(allPersonElements,"age");
        assertEquals("Wrong javaType",null, classItemsLookup.extractJavaType(nonStaticField));
    }

    private Element getElementByName(List<Element> elements, String name) {
        return elements.stream()
                .filter(e -> e.toString().equals(name))
                .findFirst().orElse(null);
    }
}
