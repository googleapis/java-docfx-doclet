package com.microsoft.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import org.apache.commons.lang3.StringUtils;

public class YamlUtil {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
            .disable(Feature.WRITE_DOC_START_MARKER)
            .disable(Feature.SPLIT_LINES)
    )
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_EMPTY);

    public static String objectToYamlString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Could not serialize object to yaml string", jpe);
        }
    }

    public static String cleanupHtml(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return text.replaceAll("<pre>([^<]+)</pre>","$1")
                .replaceAll("<pre><code>", "<pre class=\"prettyprint lang-java\"><code>")
                .replaceAll("<([A-Z][a-z]+||)>", "&lt;$1&gt;")
                .replaceAll("`([^`]+)`", "<code>$1</code>")
                .replaceAll("\\[([^]]+)]\\(([^)]+)\\)", "<a href=\"$2\">$1</a>")
                .replaceAll("\\[([^]]+)]\\[([^]]+)\\]", "<xref uid=\"$2\" data-throw-if-not-resolved=\"false\">$1</xref>")
                .replaceAll("==+([^=]+)==+", "<h2>$1</h2>");
    }
}
