package com.microsoft.build;

import static com.microsoft.build.PackageOverviewFile.extractPackageBaseURIBeforeVersion;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.CompilationRule;
import java.util.regex.Pattern;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PackageOverviewFileTest {
  @Rule public CompilationRule rule = new CompilationRule();
  private Elements elements;

  @Before
  public void setup() {
    elements = rule.getElements();
    PackageElement element = elements.getPackageElement("com.microsoft.samples");
  }

  @Test
  public void testExtractPackageBaseURIBeforeVersion() {
    Pattern pattern = Pattern.compile("(.*?)(v\\d+.*?)(?:\\.|$)");
    String packageName = "com.google.cloud.speech.v1";
    ImmutableList<Object> packageInfo_1 = extractPackageBaseURIBeforeVersion(packageName, pattern);
    assertEquals("com.google.cloud.speech.", packageInfo_1.get(0));
    assertEquals("v1", packageInfo_1.get(1));

    packageName = "com.google.cloud.speech.v1p5";
    ImmutableList<Object> packageInfo_2 = extractPackageBaseURIBeforeVersion(packageName, pattern);
    assertEquals("com.google.cloud.speech.", packageInfo_2.get(0));
    assertEquals("v1p5", packageInfo_2.get(1));

    packageName = "com.google.cloud.speech.v2.stub";
    ImmutableList<Object> packageInfo_3 = extractPackageBaseURIBeforeVersion(packageName, pattern);
    assertEquals("com.google.cloud.speech.", packageInfo_3.get(0));
    assertEquals("v2", packageInfo_3.get(1));

    packageName = "com.google.cloud.speech.velocity";
    ImmutableList<Object> packageInfo_4 = extractPackageBaseURIBeforeVersion(packageName, pattern);
    assertEquals("N/A", packageInfo_4.get(0));
    assertEquals("N/A", packageInfo_4.get(1));
  }

  @Test
  public void testWithLineBreaks() {
    String uid =
        "com.google.cloud.securitycenter.v2.AttackPathName.OrganizationLocationSimulationValuedResourceAttackPathBuilder";
    String converted = PackageOverviewFile.withLineBreaks(uid);
    assertEquals(
        "com.<wbr>google.<wbr>cloud.<wbr>securitycenter.<wbr>v2.<wbr>Attack<wbr>Path<wbr>Name.<wbr>Organization<wbr>Location<wbr>Simulation<wbr>Valued<wbr>Resource<wbr>Attack<wbr>Path<wbr>Builder",
        converted);

    String nonMatchable = "123non-matchable";
    assertEquals(nonMatchable, PackageOverviewFile.withLineBreaks(nonMatchable));
  }
}
