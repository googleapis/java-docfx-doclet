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
        String expectedWithCode = "<pre class=\"pretty-print\"><code>text</code></pre>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertEquals(expectedWithCode, YamlUtil.cleanupHtml(expectedWithCode));
    }

    @Test
    public void cleanupHtmlIncludePrettyPrintTest() {
        String expectedActual = "<pre><code>";
        String expectedResult = "<pre class=\"prettyprint lang-java\"><code>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
        assertNotEquals(expectedResult, YamlUtil.cleanupHtml("<pre>" + random + "<code>"));
        assertFalse(YamlUtil.cleanupHtml("<pre>" + random + "<code>").contains("class=\"prettyprint lang-java\""));
    }

    @Test
    public void cleanupHtmlEncodeBracketsTest() {
        String expectedActual = "<code> List<String> things = new ArrayList<>(); \n </code> <p>text</p> <Object>" ;
        String expectedResult = "<code> List&lt;String&gt; things = new ArrayList&lt;&gt;(); \n </code> <p>text</p> &lt;Object&gt;";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
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
        assertEquals("==testing==", YamlUtil.cleanupHtml("==testing=="));
        assertEquals("=======================SpeechClient=======================", "=======================SpeechClient=======================");
        assertEquals("\"scikit-learn\":\"==0.19.0\"TextTextText\"botocore\":\"==1.7.14\"", "\"scikit-learn\":\"==0.19.0\"TextTextText\"botocore\":\"==1.7.14\"");
        assertEquals("======= test1234 ===== 1234test === 1234test1234 == test =", YamlUtil.cleanupHtml("======= test1234 ===== 1234test === 1234test1234 == test ="));
        assertEquals("====== Markdown H1 Test ======", YamlUtil.cleanupHtml("====== Markdown H1 Test ======"));
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

    @Test
    public void cleanupHtmlLinkTagWithLinkTest() {
        String expectedActual = "{@link \"http://www.bad-way-to-include-link.com#section\"}";
        String expectedResult = "<a href=\"http://www.bad-way-to-include-link.com#section\">http://www.bad-way-to-include-link.com#section</a>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
    }

    @Test
    public void cleanupHtmlLinkTagNotRecognizedTest() {
        String expectedActual = "{@link WeirdLink#didntResolve(null)}";
        String expectedResult = "<xref uid=\"WeirdLink#didntResolve(null)\" data-throw-if-not-resolved=\"false\">WeirdLink#didntResolve(null)</xref>";
        String random = UUID.randomUUID().toString();

        assertEquals(expectedResult, YamlUtil.cleanupHtml(expectedActual));
        assertEquals(random + expectedResult + random, YamlUtil.cleanupHtml(random + expectedActual + random));
        assertEquals(expectedResult + random + expectedResult, YamlUtil.cleanupHtml(expectedActual + random + expectedActual));
    }
}
