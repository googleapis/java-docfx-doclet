package com.microsoft.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OptionsFileUtilTest {

    private final String PARAMS_DIR = "src/test/resources/test-doclet-params.txt";

    @Test
    public void processOptionsFile() {
        List<String> strings = Arrays.asList(OptionsFileUtil.processOptionsFile(PARAMS_DIR));

        assertTrue("Wrong result", strings.contains("-doclet"));
        assertTrue("Wrong result", strings.contains("com.microsoft.doclet.DocFxDoclet"));

        assertTrue("Wrong result", strings.contains("-sourcepath"));
        assertTrue("Wrong result", strings.contains("./src/test/java"));

        assertTrue("Wrong result", strings.contains("-outputpath"));
        assertTrue("Wrong result", strings.contains("./target/test-out"));

        assertTrue("Wrong result", strings.contains("-encoding"));
        assertTrue("Wrong result", strings.contains("UTF-8"));

        assertTrue("Wrong result", strings.contains("-projectname"));
        assertTrue("Wrong result", strings.contains("google-cloud-project-parent"));

        assertTrue("Wrong result", strings.contains("-excludepackages"));
        assertTrue("Wrong result", strings.contains("com\\.microsoft\\.samples\\.someexcludedpack.*:com\\.microsoft\\.samples\\.someunexistingpackage"));

        assertTrue("Wrong result", strings.contains("-excludeclasses"));
        assertTrue("Wrong result", strings.contains("com\\.microsoft\\.samples\\.subpackage\\.SomeExcluded.*:com\\.microsoft\\.samples\\.subpackage\\.SomeUnexistingClass"));

        assertTrue("Wrong result", strings.contains("-subpackages"));
        assertTrue("Wrong result", strings.contains("com.microsoft.samples"));
    }
}
