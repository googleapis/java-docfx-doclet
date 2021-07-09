package com.microsoft.util;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

    @Test
    public void convertHtmlToMarkdown() throws IOException {
        String text = FileUtils.readFileToString(new File("target/test-classes/html2md/initial.html"), UTF_8);
        String expectedResult = FileUtils.readFileToString(new File("target/test-classes/html2md/converted.md"), UTF_8);

        String result = YamlUtil.convertHtmlToMarkdown(text);

        assertEquals("Wrong result", result, expectedResult);
    }

    @Test
    public void convertHtmlToMarkdownForBlankParam() {
        assertNull("Wrong result for null", YamlUtil.convertHtmlToMarkdown(null));
        assertEquals("Wrong result for empty string", YamlUtil.convertHtmlToMarkdown(""), "");
    }

    private MetadataFileItem buildMetadataFileItem(int seed) {
        MetadataFileItem metadataFileItem = new MetadataFileItem("Some uid " + seed);
        metadataFileItem.setId("Some id" + seed);
        metadataFileItem.setHref("Some href" + seed);
        metadataFileItem.setParameters(Collections.singletonList(
                new MethodParameter("Some id " + seed, "Some type " + seed, "Some desc " + seed)));

        return metadataFileItem;
    }
}
