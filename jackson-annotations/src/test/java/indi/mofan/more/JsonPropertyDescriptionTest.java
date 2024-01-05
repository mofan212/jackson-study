package indi.mofan.more;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.factories.SchemaFactoryWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2024/1/5 15:35
 */
public class JsonPropertyDescriptionTest implements WithAssertions {

    @Getter
    @Setter
    private static class PropertyDescriptionBean {
        private int id;
        @JsonPropertyDescription("This is a description of the name property")
        private String name;
    }

    @Test
    @SneakyThrows
    public void test() {
        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper();
        JsonMapper mapper = JsonMapper.builder().build();
        mapper.acceptJsonFormatVisitor(PropertyDescriptionBean.class, wrapper);
        JsonSchema jsonSchema = wrapper.finalSchema();
        String result = mapper.writeValueAsString(jsonSchema);
        // language=JSON
        String expectJson = """
                {
                  "type": "object",
                  "id": "urn:jsonschema:indi:mofan:more:JsonPropertyDescriptionTest:PropertyDescriptionBean",
                  "properties": {
                    "id": {
                      "type": "integer"
                    },
                    "name": {
                      "type": "string",
                      "description": "This is a description of the name property"
                    }
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
