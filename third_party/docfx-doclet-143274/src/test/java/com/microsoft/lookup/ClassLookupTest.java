package com.microsoft.lookup;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.Status;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.ElementUtil;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassLookupTest {

  @Rule public CompilationRule rule = new CompilationRule();
  private Elements elements;
  private ClassLookup classLookup;
  private DocletEnvironment environment;
  private DocTrees docTrees;
  private DocTree docTree;
  private DocCommentTree docCommentTree;
  private DeprecatedTree deprecatedTree;
  private TextTree textTree;
  private TypeMirror typeMirror;
  private ClassItemsLookup classItemsLookup;

  @Before
  public void setup() {
    elements = rule.getElements();
    environment = Mockito.mock(DocletEnvironment.class);
    classLookup = new ClassLookup(environment, Mockito.mock(ElementUtil.class));
    docTrees = Mockito.mock(DocTrees.class);
    docTree = Mockito.mock(DocTree.class);
    docCommentTree = Mockito.mock(DocCommentTree.class);
    deprecatedTree = Mockito.mock(DeprecatedTree.class);
    textTree = Mockito.mock(TextTree.class);
    typeMirror = Mockito.mock(TypeMirror.class);
    classItemsLookup = new ClassItemsLookup(environment, Mockito.mock(ElementUtil.class));

    when(environment.getDocTrees()).thenReturn(docTrees);
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
    TypeElement element =
        elements.getTypeElement(
            "com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

    String result = classLookup.determineSuperclass(element);

    assertEquals(
        "Wrong result",
        result,
        "java.lang.Enum<com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender>");
  }

  @Test
  public void determineClassContent() {
    TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
    ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

    classLookup.populateContent(element, "SuperHero", container);

    assertEquals(
        "Wrong content",
        container.getContent(),
        "public class SuperHero extends Person implements Serializable, Cloneable");

    assertTrue(
        "Wrong set of interfaces", container.getInterfaces().contains("java.io.Serializable"));
    assertTrue(
        "Wrong set of interfaces", container.getInterfaces().contains("java.lang.Cloneable"));
  }

  @Test
  public void determineClassContentForInterface() {
    TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");
    ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

    classLookup.populateContent(element, "Display<T,R>", container);

    assertEquals(
        "Wrong content",
        container.getContent(),
        "public interface Display<T,R> extends Serializable, List<Person<T>>");

    assertTrue(
        "Wrong set of interfaces", container.getInterfaces().contains("java.io.Serializable"));
    assertTrue(
        "Wrong set of interfaces",
        container
            .getInterfaces()
            .contains("java.util.List<com.microsoft.samples.subpackage.Person<T>>"));
  }

  @Test
  public void determineClassContentForEnum() {
    TypeElement element =
        elements.getTypeElement(
            "com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");
    ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

    classLookup.populateContent(element, "Person.IdentificationInfo.Gender", container);

    assertEquals(
        "Wrong content",
        container.getContent(),
        "public enum Person.IdentificationInfo.Gender extends Enum<Person.IdentificationInfo.Gender>");
  }

  @Test
  public void determineClassContentForStaticClass() {
    TypeElement element =
        elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");
    ExtendedMetadataFileItem container = new ExtendedMetadataFileItem("UID");

    classLookup.populateContent(element, "Person.IdentificationInfo", container);

    assertEquals(
        "Wrong content", container.getContent(), "public static class Person.IdentificationInfo");
  }

  @Test
  public void determineTypeForInterface() {
    TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

    assertEquals(classLookup.determineType(element), "Interface");
  }

  @Test
  public void determineTypeForEnum() {
    TypeElement element =
        elements.getTypeElement(
            "com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

    assertEquals(classLookup.determineType(element), "Enum");
  }

  @Test
  public void determineTypeForClass() {
    TypeElement element =
        elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

    assertEquals(classLookup.determineType(element), "Class");
  }

  @Test
  public void extractStatus_deprecated() {
    TypeElement element =
        elements.getTypeElement(
            "com.microsoft.samples.agreements.AgreementDetailsCollectionOperations");

    String result = classLookup.extractStatus(element);

    assertEquals("Wrong description", result, Status.DEPRECATED.toString());
  }

  @Test
  public void extractStatus_notDeprecated() {
    TypeElement element =
        elements.getTypeElement("com.microsoft.samples.agreements.AgreementMetaData");

    String result = classLookup.extractStatus(element);

    assertNull("Wrong description", result);
  }

  @Test
  public void testExtractJavaType() {
    TypeElement typeElement =
        elements.getTypeElement("com.microsoft.samples.google.ValidationException");
    assertEquals("Wrong javaType", classLookup.extractJavaType(typeElement), "exception");

    typeElement = elements.getTypeElement("com.microsoft.samples.google.RecognitionAudio");
    assertNull("Wrong javaType", classLookup.extractJavaType(typeElement));

    typeElement = elements.getTypeElement("com.microsoft.samples.google.BetaApi");
    assertEquals("Wrong javaType", classLookup.extractJavaType(typeElement), "annotationtype");

    typeElement = elements.getTypeElement("com.microsoft.samples.IPartner");
    assertNull("Wrong javaType", classLookup.extractJavaType(typeElement));
  }

  @Test
  public void testExtractStatus_class_beta() {
    TypeElement betaApi = elements.getTypeElement("com.microsoft.samples.google.BetaApi");
    assertThat(classLookup.extractStatus(betaApi)).isEqualTo("beta");
  }

  @Test
  public void testExtractStatus_method_internal() {
    TypeElement stub =
        elements.getTypeElement("com.microsoft.samples.google.v1.stub.HttpJsonSpeechStub");

    Element getMethodDescriptors =
        stub.getEnclosedElements().stream()
            .filter(element -> element.getSimpleName().toString().equals("getMethodDescriptors"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to find getMethodDescriptors()"));

    assertThat(classItemsLookup.extractStatus(getMethodDescriptors)).isEqualTo("internal");
  }

  @Test
  public void testExtractStatus_class_obsolete() {
    TypeElement client =
        elements.getTypeElement("com.microsoft.samples.google.v1beta.SpeechClient");
    assertThat(classLookup.extractStatus(client)).isEqualTo("obsolete");
  }

  @Test
  public void testExtractStatus_class_internalExtensionOnly() {
    TypeElement settings = elements.getTypeElement("com.microsoft.samples.google.SpeechSettings");
    assertThat(classLookup.extractStatus(settings)).isEqualTo("internal");
  }
}
