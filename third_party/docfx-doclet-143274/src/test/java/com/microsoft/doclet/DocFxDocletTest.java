package com.microsoft.doclet;

import com.microsoft.doclet.DocFxDoclet.CustomOption;
import com.microsoft.doclet.DocFxDoclet.FakeOptionForCompatibilityWithStandardDoclet;
import jdk.javadoc.doclet.Doclet.Option.Kind;
import org.junit.Before;
import org.junit.Test;

import javax.lang.model.SourceVersion;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DocFxDocletTest {

    private DocFxDoclet doclet;

    @Before
    public void setup() {
        doclet = new DocFxDoclet();
    }

    @Test
    public void getSupportedSourceVersion() {
        assertEquals("Wrong version used", doclet.getSupportedSourceVersion(), SourceVersion.latest());
    }

    @Test
    public void getDocletName() {
        assertEquals("Wrong doclet name", doclet.getName(), "DocFxDoclet");
    }

    @Test
    public void testCustomOptionCreation() {
        String description = "Some desc";
        List<String> names = Arrays.asList("name 1", "name 2");
        String params = "Some params";

        CustomOption option = new CustomOption(description, names, params) {
            @Override
            public boolean process(String option, List<String> arguments) {
                return false;
            }
        };

        assertEquals("Wrong args count", option.getArgumentCount(),1);
        assertEquals("Wrong description", option.getDescription(), description);
        assertEquals("Wrong kind", option.getKind(), Kind.STANDARD);
        assertEquals("Wrong names", option.getNames(), names);
        assertEquals("Wrong params", option.getParameters(), params);
    }

    @Test
    public void testFakeOptionCreation() {
        FakeOptionForCompatibilityWithStandardDoclet option =
            new FakeOptionForCompatibilityWithStandardDoclet("Some description", "title");

        assertEquals("Wrong args count", option.getArgumentCount(), 1);
        assertEquals("Wrong description", option.getDescription(), "Some description");
        assertEquals("Wrong kind", option.getKind(), Kind.STANDARD);
        assertEquals("Wrong names", option.getNames(), Arrays.asList("title"));
        assertEquals("Wrong params", option.getParameters(),"none");
    }
}
