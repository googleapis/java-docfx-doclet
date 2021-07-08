package com.microsoft.lookup;

import com.google.testing.compile.CompilationRule;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.PackageElement;
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

        assertEquals( "Wrong result", result, "package com.microsoft.samples");
    }
}
