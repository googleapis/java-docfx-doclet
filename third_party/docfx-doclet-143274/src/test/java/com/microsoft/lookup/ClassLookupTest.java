package com.microsoft.lookup;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.TypeParameter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ClassLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private ClassLookup classLookup;
    private DocletEnvironment environment;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        classLookup = new ClassLookup(environment);
    }

    @Test
    public void determineTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = classLookup.determineTypeParameters(element);

        assertEquals("Wrong type params size", result.size(), 1);
        assertEquals("Wrong type parameter id", result.get(0).getId(), "T");
    }

    @Test
    public void determineSuperclass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        String result = classLookup.determineSuperclass(element);

        assertEquals("Wrong result", result, "java.lang.Object");
    }

    @Test
    public void determineSuperclassForChildClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = classLookup.determineSuperclass(element);

        assertEquals("Wrong result", result, "com.microsoft.samples.subpackage.Person");
    }

    @Test
    public void determineSuperclassForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        String result = classLookup.determineSuperclass(element);

        assertEquals("Wrong result", result,
            "java.lang.Enum<com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender>");
    }

    @Test
    public void determineClassContent() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

        classLookup.populateContent(element, "SuperHero", container);

        assertEquals("Wrong content", container.getContent(),
            "public class SuperHero extends Person implements Serializable, Cloneable");


        assertTrue("Wrong set of interfaces", container.getInterfaces().contains("java.io.Serializable"));
        assertTrue("Wrong set of interfaces", container.getInterfaces().contains("java.lang.Cloneable"));
    }

    @Test
    public void determineClassContentForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");
        ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

        classLookup.populateContent(element, "Display<T,R>", container);

        assertEquals("Wrong content", container.getContent(),
            "public interface Display<T,R> extends Serializable, List<Person<T>>");


        assertTrue("Wrong set of interfaces", container.getInterfaces().contains("java.io.Serializable"));
        assertTrue("Wrong set of interfaces", container.getInterfaces().contains("java.util.List<com.microsoft.samples.subpackage.Person<T>>"));
    }

    @Test
    public void determineClassContentForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");
        ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

        classLookup.populateContent(element, "Person.IdentificationInfo.Gender", container);

        assertEquals("Wrong content", container.getContent(),
            "public enum Person.IdentificationInfo.Gender extends Enum<Person.IdentificationInfo.Gender>");
    }

    @Test
    public void determineClassContentForStaticClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");
        ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

        classLookup.populateContent(element, "Person.IdentificationInfo", container);

        assertEquals("Wrong content", container.getContent(), "public static class Person.IdentificationInfo");
    }

    @Test
    public void determineTypeForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        assertEquals(classLookup.determineType(element), "Interface");
    }

    @Test
    public void determineTypeForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        assertEquals(classLookup.determineType(element), "Enum");
    }

    @Test
    public void determineTypeForClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        assertEquals(classLookup.determineType(element), "Class");
    }
}
