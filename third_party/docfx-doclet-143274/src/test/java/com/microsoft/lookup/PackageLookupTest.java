package com.microsoft.lookup;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.PackageLookup.PackageGroup;
import com.microsoft.model.Status;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PackageLookupTest {

  @Rule public CompilationRule rule = new CompilationRule();
  private Elements elements;
  private PackageLookup packageLookup;
  private DocletEnvironment environment;

  @Before
  public void setup() {
    elements = rule.getElements();
    environment = Mockito.mock(DocletEnvironment.class);
    packageLookup = new PackageLookup(environment);
  }

  @Test
  public void extractPackageContent() {
    PackageElement element = elements.getPackageElement("com.microsoft.samples");

    String result = packageLookup.determinePackageContent(element);

    assertEquals("Wrong result", result, "package com.microsoft.samples");
  }

  @Test
  public void extractPackageStatus() {
    PackageElement beta = elements.getPackageElement("com.microsoft.samples.google.v1beta");
    PackageElement alpha = elements.getPackageElement("com.microsoft.samples.google.v1p1alpha");
    PackageElement v1 = elements.getPackageElement("com.microsoft.samples.google.v1");

    String resultA = packageLookup.extractStatus(alpha);
    String resultB = packageLookup.extractStatus(beta);
    String resultV1 = packageLookup.extractStatus(v1);

    assertThat(resultA).isEqualTo(Status.ALPHA.toString());
    assertThat(resultB).isEqualTo(Status.BETA.toString());
    assertThat(resultV1).isNull();
  }

  @Test
  public void testExtractJavaType() {
    PackageElement packageElement =
        elements.getPackageElement("com.microsoft.samples.google.v1beta");
    assertEquals("Wrong javaType", packageLookup.extractJavaType(packageElement), "package");
  }

  @Test
  public void testGroupVersions() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1p1alpha"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"),
            elements.getPackageElement("com.microsoft.samples.google.v1"),
            elements.getPackageElement("com.microsoft.samples.google"),
            elements.getPackageElement("com.microsoft.samples"));

    Multimap<String, PackageElement> groupedPackages = packageLookup.groupVersions(packages);

    assertThat(groupedPackages.keys()).hasCount("com.microsoft.samples.google.v#", 3);
    assertThat(groupedPackages.keys()).hasCount("com.microsoft.samples.google", 1);
    assertThat(groupedPackages.keys()).hasCount("com.microsoft.samples", 1);
  }

  @Test
  public void testRecommendation() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1p1alpha"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"));

    PackageElement recommended = packageLookup.getRecommended(packages);

    assertThat(String.valueOf(recommended.getQualifiedName()))
        .isEqualTo("com.microsoft.samples.google.v1p1alpha");
  }

  @Test
  public void testRecommendation_SinglePackage() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(elements.getPackageElement("com.microsoft.samples.google.v1beta"));

    PackageElement recommended = packageLookup.getRecommended(packages);

    assertThat(String.valueOf(recommended.getQualifiedName()))
        .isEqualTo("com.microsoft.samples.google.v1beta");
  }

  @Test
  public void testRecommendation_WithUnversionedPackageCollection() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google"),
            elements.getPackageElement("com.microsoft.samples"));

    assertThrows(IllegalStateException.class, () -> packageLookup.getRecommended(packages));
  }

  @Test
  public void testRecommendation_WithDuplicates() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1beta"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"));

    assertThrows(IllegalArgumentException.class, () -> packageLookup.getRecommended(packages));
  }

  @Test
  public void testOrganize() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1p1alpha"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"),
            elements.getPackageElement("com.microsoft.samples.google.v1"),
            elements.getPackageElement("com.microsoft.samples.google"),
            elements.getPackageElement("com.microsoft.samples"));

    ImmutableListMultimap<PackageGroup, PackageElement> organized =
        packageLookup.organize(packages);

    assertThat(organized.keys()).hasCount(PackageGroup.VISIBLE, 3);
    assertThat(organized.keys()).hasCount(PackageGroup.OLDER_AND_PRERELEASE, 2);

    assertThat(toPackageNames(organized.get(PackageGroup.VISIBLE)))
        .containsExactly(
            "com.microsoft.samples",
            "com.microsoft.samples.google",
            "com.microsoft.samples.google.v1");

    assertThat(toPackageNames(organized.get(PackageGroup.OLDER_AND_PRERELEASE)))
        .containsExactly(
            "com.microsoft.samples.google.v1beta", "com.microsoft.samples.google.v1p1alpha");
  }

  @Test
  public void testOrganize_WithoutReleasePackage() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1p1alpha"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"),
            elements.getPackageElement("com.microsoft.samples.google"),
            elements.getPackageElement("com.microsoft.samples"));

    ImmutableListMultimap<PackageGroup, PackageElement> organized =
        packageLookup.organize(packages);

    assertThat(toPackageNames(organized.get(PackageGroup.VISIBLE)))
        .containsExactly(
            "com.microsoft.samples",
            "com.microsoft.samples.google",
            "com.microsoft.samples.google.v1p1alpha");

    assertThat(toPackageNames(organized.get(PackageGroup.OLDER_AND_PRERELEASE)))
        .containsExactly("com.microsoft.samples.google.v1beta");
  }

  @Test
  public void testFindStubPackage() {
    ImmutableList<PackageElement> packages =
        ImmutableList.of(
            elements.getPackageElement("com.microsoft.samples.google.v1"),
            elements.getPackageElement("com.microsoft.samples.google.v1.stub"),
            elements.getPackageElement("com.microsoft.samples.google.v1beta"),
            elements.getPackageElement("com.microsoft.samples.google"));

    Optional<PackageElement> foundStubPackage =
        packageLookup.findStubPackage(
            elements.getPackageElement("com.microsoft.samples.google.v1"), packages);
    assertThat(foundStubPackage.isPresent()).isTrue();
    assertThat(toPackageName(foundStubPackage.get()))
        .isEqualTo("com.microsoft.samples.google.v1.stub");

    Optional<PackageElement> notFoundStubPackageOfStubPackage =
        packageLookup.findStubPackage(
            elements.getPackageElement("com.microsoft.samples.google.v1.stub"), packages);
    assertThat(notFoundStubPackageOfStubPackage.isPresent()).isFalse();

    Optional<PackageElement> notFoundStubPackage =
        packageLookup.findStubPackage(
            elements.getPackageElement("com.microsoft.samples.google"), packages);
    assertThat(notFoundStubPackage.isPresent()).isFalse();
  }

  @Test
  public void testIsApiStubPackage() {
    assertThat(
            packageLookup.isApiVersionStubPackage(
                elements.getPackageElement("com.microsoft.samples.google.v1")))
        .isFalse();
    assertThat(
            packageLookup.isApiVersionStubPackage(
                elements.getPackageElement("com.microsoft.samples.google.v1.stub")))
        .isTrue();

    // False due to not being an API version package, even though it ends in .stub
    assertThat(packageLookup.isApiVersionStubPackageName("com.microsoft.samples.google.stub"))
        .isFalse();
  }

  private List<String> toPackageNames(List<PackageElement> packages) {
    return packages.stream().map(this::toPackageName).collect(Collectors.toList());
  }

  private String toPackageName(PackageElement pkg) {
    return String.valueOf(pkg.getQualifiedName());
  }
}
