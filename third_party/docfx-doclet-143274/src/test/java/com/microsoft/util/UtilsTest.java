package com.microsoft.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.testing.compile.CompilationRule;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

  @Rule public CompilationRule rule = new CompilationRule();
  private Elements elements;
  private List<Element> allElements;

  @Before
  public void setup() {
    elements = rule.getElements();
    Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");
    allElements = element.getEnclosedElements().stream().collect(Collectors.toList());
  }

  // Test isPackagePrivate() method
  @Test
  public void isPackagePrivate_True_PackagePrivateMethod() {
    Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHobby()");
    assertTrue(Utils.isPackagePrivate(method));
  }

  @Test
  public void isPackagePrivate_True_PackagePrivateField() {
    Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "hobby");
    assertTrue(Utils.isPackagePrivate(field));
  }

  // Test isPrivate() method
  @Test
  public void isPrivate_True_PrivateMethod() {
    Element method =
        getElementByKindAndName(allElements, ElementKind.METHOD, "setHobby(java.lang.String)");
    assertTrue(Utils.isPrivate(method));
  }

  @Test
  public void isPrivate_True_PrivateField() {
    Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "uniquePower");
    assertTrue(Utils.isPrivate(field));
  }

  // Test isPrivateOrPackagePrivate() method
  @Test
  public void isPrivateOrPackagePrivate_True_PackagePrivateMethod() {
    Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHobby()");
    assertTrue(Utils.isPrivateOrPackagePrivate(method));
  }

  @Test
  public void isPrivateOrPackagePrivate_True_PrivateFiled() {
    Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "uniquePower");
    assertTrue(Utils.isPrivateOrPackagePrivate(field));
  }

  @Test
  public void isPrivateOrPackagePrivate_False_PublicMethod() {
    Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getUniquePower()");
    assertFalse(Utils.isPrivateOrPackagePrivate(method));
  }

  @Test
  public void isPrivateOrPackagePrivate_False_PublicField() {
    Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "SOME_PUBLIC_STRING");
    assertFalse(Utils.isPrivateOrPackagePrivate(field));
  }

  @Test
  public void isPrivateOrPackagePrivate_False_ProtectedMethod() {
    Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHealth()");
    assertFalse(Utils.isPrivateOrPackagePrivate(method));
  }

  @Test
  public void getRecommendedPackageBaseURI() {
    String packageName = "com.google.cloud.speech.v1";
    String[] recommendedPackageName = Utils.extractPackageBaseURIBeforeVersion(packageName);
    assertEquals("com.google.cloud.speech.", recommendedPackageName[0]);
    assertEquals("v1", recommendedPackageName[1]);

    packageName = "com.google.cloud.speech.v1p5";
    recommendedPackageName = Utils.extractPackageBaseURIBeforeVersion(packageName);
    assertEquals("com.google.cloud.speech.", recommendedPackageName[0]);
    assertEquals("v1p5", recommendedPackageName[1]);

    packageName = "com.google.cloud.speech.v2.stub";
    recommendedPackageName = Utils.extractPackageBaseURIBeforeVersion(packageName);
    assertEquals("com.google.cloud.speech.", recommendedPackageName[0]);
    assertEquals("v2", recommendedPackageName[1]);

    packageName = "com.google.cloud.speech.velocity";
    recommendedPackageName = Utils.extractPackageBaseURIBeforeVersion(packageName);
    assertEquals("N/A", recommendedPackageName[0]);
    assertEquals("N/A", recommendedPackageName[1]);
  }

  private Element getElementByKindAndName(
      List<? extends Element> elements, ElementKind elementKind, String name) {
    return elements.stream()
        .filter(e -> e.toString().equals(name))
        .filter(e -> e.getKind() == elementKind)
        .findFirst()
        .orElse(null);
  }
}
