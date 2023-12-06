package indi.mofan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;

/**
 * @author mofan
 * @date 2023/12/6 11:28
 */
public class IgnoreNullTest implements WithAssertions {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MyDto {
        private String stringValue;
        private Integer intValue;
    }

    /**
     * <a href="https://www.baeldung.com/jackson-ignore-null-fields#globally">Ignore Null Fields Globally</a>
     */
    @Test
    @SneakyThrows
    public void testIgnoreNullGlobally() {
        MyDto dto = new MyDto(null, 212);

        JsonMapper mapper = JsonMapper.builder()
                // 排除 null 值字段
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();

        String result = mapper.writeValueAsString(dto);
        // language=JSON
        String expectJson = """
                {
                  "intValue": 212
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    /**
     * <a href="https://www.baeldung.com/jackson-map-null-values-or-null-key">Jackson – Working With Maps and Nulls</a>
     */
    @Test
    @SneakyThrows
    public void testWorkingWithMapsAndNulls() {
        var nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, 212);

        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.writeValueAsString(nullKeyMap))
                .withMessageContaining("Null key for a Map not allowed in JSON");

        JsonMapper jsonMapper = JsonMapper.builder().build();
        jsonMapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
        String result = jsonMapper.writeValueAsString(nullKeyMap);
        String expectJson = """
                {
                  "": 212
                }""";
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class NullKeySerializer extends StdSerializer<Object> {
        @Serial
        private static final long serialVersionUID = 1619906623475807318L;

        public NullKeySerializer() {
            this(null);
        }

        public NullKeySerializer(Class<Object> t) {
            super(t);
        }

        @Override
        public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused)
                throws IOException {
            // null key 转为空字符串
            jsonGenerator.writeFieldName("");
        }
    }
}
