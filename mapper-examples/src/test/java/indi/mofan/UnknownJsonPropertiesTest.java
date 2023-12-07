package indi.mofan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/7 14:59
 * @link <a href="https://www.baeldung.com/jackson-deserialize-json-unknown-properties">Jackson Unmarshalling JSON with Unknown Properties</a>
 */
public class UnknownJsonPropertiesTest implements WithAssertions {
    @Getter
    @Setter
    @NoArgsConstructor
    private static class MyDto {
        private String stringValue;
        private Integer intValue;
        private Boolean booleanValue;
    }

    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MyDtoIgnoreUnknown extends MyDto {
    }

    @Test
    @SneakyThrows
    public void testUnmarshalUnknownJsonProperties() {
        //language=JSON
        String targetJson = """
                {
                  "stringValue": "a",
                  "intValue": 1,
                  "booleanValue": true,
                  "stringValue2": "something"
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();
        // stringValue2 字段在类中不存在，所以抛出 UnrecognizedPropertyException 异常
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(targetJson, MyDto.class));

        JsonMapper jsonMapper = JsonMapper.builder()
                // 关闭因为不存在的字段导致的异常
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        MyDto value = jsonMapper.readValue(targetJson, MyDto.class);
        assertThat(value).extracting(MyDto::getStringValue, MyDto::getIntValue, MyDto::getBooleanValue)
                .containsExactly("a", 1, true);

        // 在类级别上处理
        value = mapper.readValue(targetJson, MyDtoIgnoreUnknown.class);
        assertThat(value).extracting(MyDto::getStringValue, MyDto::getIntValue, MyDto::getBooleanValue)
                .containsExactly("a", 1, true);
    }

    @Test
    @SneakyThrows
    public void testUnmarshallIncompleteJson() {
        //language=JSON
        String targetJson = """
                {
                  "stringValue": "a",
                  "booleanValue": true
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();
        MyDto value = mapper.readValue(targetJson, MyDto.class);
        // 类中存在，JSON 中不存在的数据，反序列化时不会抛异常
        assertThat(value).extracting(MyDto::getStringValue, MyDto::getIntValue, MyDto::getBooleanValue)
                .containsExactly("a", null, true);
    }
}
