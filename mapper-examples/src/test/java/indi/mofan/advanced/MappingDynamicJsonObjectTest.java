package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mofan
 * @date 2024/2/5 11:04
 * @link <a href="https://www.baeldung.com/jackson-mapping-dynamic-object">Mapping a Dynamic JSON Object with Jackson</a>
 */
public class MappingDynamicJsonObjectTest implements WithAssertions {
    //language=JSON
    static final String JSON_OBJECT = """
            {
              "name": "Pear yPhone 72",
              "category": "cellphone",
              "details": {
                "displayAspectRatio": "97:3",
                "audioConnector": "none"
              }
            }
            """;

    static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Getter
    @Setter
    static class ProductWithJsonNode {
        String name;
        String category;
        JsonNode details;
    }

    @Test
    @SneakyThrows
    public void testJsonNode() {
        // 使用 JsonNode 虽然能够处理，但是会强依赖 Jackson 的 JsonNode
        ProductWithJsonNode product = MAPPER.readValue(JSON_OBJECT, ProductWithJsonNode.class);

        assertThat(product.getName()).isEqualTo("Pear yPhone 72");
        assertThat(product.getDetails().get("audioConnector").asText()).isEqualTo("none");
    }

    @Getter
    @Setter
    static class ProductWithMap {
        String name;
        String category;
        Map<String, Object> details;
    }

    @Test
    @SneakyThrows
    public void testMap() {
        ProductWithMap product = MAPPER.readValue(JSON_OBJECT, ProductWithMap.class);

        assertThat(product.getName()).isEqualTo("Pear yPhone 72");
        assertThat(product.getDetails().get("audioConnector")).isEqualTo("none");
    }

    @Getter
    static class ProductWithJsonAnySetter {
        @Setter
        String name;
        @Setter
        String category;

        Map<String, Object> details = new LinkedHashMap<>();

        @JsonAnySetter
        void setDetail(String key, Object value) {
            details.put(key, value);
        }
    }
    
    @Test
    @SneakyThrows
    public void testJsonAnySetterAnnotation() {
        // language=JSON
        String flattenJsonObject = """
                {
                  "name": "Pear yPhone 72",
                  "category": "cellphone",
                  "displayAspectRatio": "97:3",
                  "audioConnector": "none"
                }
                """;
        ProductWithJsonAnySetter product = MAPPER.readValue(flattenJsonObject, ProductWithJsonAnySetter.class);

        assertThat(product.getName()).isEqualTo("Pear yPhone 72");
        assertThat(product.getDetails().get("audioConnector")).isEqualTo("none");
    }
}
