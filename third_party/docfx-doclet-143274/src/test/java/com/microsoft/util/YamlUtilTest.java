package com.microsoft.util;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class YamlUtilTest {

    @Test
    public void objectToYamlString() {
        MetadataFile metadataFile = new MetadataFile("", "SomeFileName");
        metadataFile.getItems().add(buildMetadataFileItem(3));
        metadataFile.getReferences().add(buildMetadataFileItem(5));

        String result = YamlUtil.objectToYamlString(metadataFile);

        assertEquals("Wrong result", result, ""
                + "items:\n"
                + "- uid: \"Some uid 3\"\n"
                + "  id: \"Some id3\"\n"
                + "  href: \"Some href3\"\n"
                + "  syntax:\n"
                + "    parameters:\n"
                + "    - id: \"Some id 3\"\n"
                + "      type: \"Some type 3\"\n"
                + "      description: \"Some desc 3\"\n"
                + "references:\n"
                + "- uid: \"Some uid 5\"\n"
                + "  id: \"Some id5\"\n"
                + "  href: \"Some href5\"\n"
                + "  syntax:\n"
                + "    parameters:\n"
                + "    - id: \"Some id 5\"\n"
                + "      type: \"Some type 5\"\n"
                + "      description: \"Some desc 5\"\n");
    }

    private MetadataFileItem buildMetadataFileItem(int seed) {
        MetadataFileItem metadataFileItem = new MetadataFileItem("Some uid " + seed);
        metadataFileItem.setId("Some id" + seed);
        metadataFileItem.setHref("Some href" + seed);
        metadataFileItem.setParameters(Collections.singletonList(
                new MethodParameter("Some id " + seed, "Some type " + seed, "Some desc " + seed)));

        return metadataFileItem;
    }


    @Test
    public void cleanupHtmlRemoveLonePreTagsTest() {
        String expectedActual = "<pre>text</pre>";
        String expectedResult = "text";
        String expectedWithCode = "<pre><code class=\"pretty-print\">text</code></pre>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertEquals(expectedWithCode, YamlUtil.cleanupHtml(expectedWithCode));
    }

    @Test
    public void cleanupHtmlIncludePrettyPrintTest() {
        String expectedActual = "<pre><code>";
        String expectedResult = "<pre><code class=\"pretty-print\">";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertNotEquals(expectedResult, YamlUtil.cleanupHtml("<pre>" + random + "<code>"));
        assertFalse(YamlUtil.cleanupHtml("<pre>" + random + "<code>").contains("class=\"pretty-print\""));
    }

    @Test
    public void cleanupHtmlAddCodeTagsTest() {
        String expectedActual = "`text`";
        String expectedResult = "<code>text</code>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertEquals("`" + expectedResult, YamlUtil.cleanupHtml("`" + expectedActual));
        assertFalse(YamlUtil.cleanupHtml("`" + random).contains("<code>"));
    }

    @Test
    public void cleanupHtmlAddHrefTagsTest() {
        String expectedActual = "[text](link)";
        String expectedResult = "<a href=\"link\">text</a>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertEquals("[text]](link)", YamlUtil.cleanupHtml("[text]](link)"));
        assertFalse(YamlUtil.cleanupHtml("[text(link)]").contains("href"));
    }

    @Test
    public void cleanupHtmlEqualTitlesTest() {
        String expectedActual = "======================= SpeechClient =======================";
        String expectedResult = "<h2> SpeechClient </h2>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertEquals("= text =", YamlUtil.cleanupHtml("= text ="));
    }

    @Test
    public void cleanupHtmlReferenceTest() {
        String expectedActual = "[KeyRing][google.cloud.kms.v1.KeyRing]";
        String expectedResult = "<xref uid=\"google.cloud.kms.v1.KeyRing\" data-throw-if-not-resolved=\"false\">KeyRing</xref>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));

        assertEquals("[uid]][text]", YamlUtil.cleanupHtml("[uid]][text]"));
        assertFalse(YamlUtil.cleanupHtml("[text[uid]]").contains("xref"));
    }
}
