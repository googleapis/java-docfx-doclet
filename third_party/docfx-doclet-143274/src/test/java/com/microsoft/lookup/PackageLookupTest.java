package com.microsoft.lookup;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.Status;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PackageLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
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
        PackageElement elementBeta = elements.getPackageElement("com.microsoft.samples.google.v1beta");
        PackageElement elementAlpha = elements.getPackageElement("com.microsoft.samples.google.v1p1alpha");

        String resultA = packageLookup.extractStatus(elementAlpha);
        String resultB = packageLookup.extractStatus(elementBeta);

        assertEquals("Wrong result", resultA, Status.ALPHA.toString());
        assertEquals("Wrong result", resultB, Status.BETA.toString());
    }

    @Test
    public void testExtractJavaType() {
        PackageElement packageElement = elements.getPackageElement("com.microsoft.samples.google.v1beta");
        assertEquals("Wrong javaType", packageLookup.extractJavaType(packageElement), "package");
    }
}
