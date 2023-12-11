package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/11 20:01
 */
public class SoluteJsonMappingExceptionTest implements WithAssertions {
    private static class MyDtoNoAccessors {
        String stringValue;
        int intValue;
        boolean booleanValue;
    }

    @Test
    @SneakyThrows
    public void testGivenObjectHasNoAccessors() {
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.writeValueAsString(new MyDtoNoAccessors()));
    }
    
    @Test
    @SneakyThrows
    public void testGloballyDetectAllFields() {
        JsonMapper mapper = JsonMapper.builder()
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .build();

        String result = mapper.writeValueAsString(new MyDtoNoAccessors());
        String exceptJson = """
                {
                  "stringValue": null,
                  "intValue": 0,
                  "booleanValue": false
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(exceptJson);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class MyDtoNoAccessorsWithAnyVisibility {
        String stringValue;
        int intValue;
        boolean booleanValue;
    }

    @Test
    @SneakyThrows
    public void testDetectAllFieldsAtClassLevel() {
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(new MyDtoNoAccessorsWithAnyVisibility());
        String exceptJson = """
                {
                  "stringValue": null,
                  "intValue": 0,
                  "booleanValue": false
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(exceptJson);
    }
}
