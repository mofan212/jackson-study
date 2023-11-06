package indi.mofan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/11/6 22:02
 */
public class JacksonDeserializeTest implements WithAssertions {

    static JsonMapper mapper = JsonMapper.builder().build();

    // language=JSON
    static final String JSON = """
            {
              "iAge": 21
            }
                        """;

    @Getter
    @Setter
    static class MyFirstData {
        private Integer iAge;
    }

    @Test
    @SneakyThrows
    public void testDeserializeFirstData() {
        // 抛出异常
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(JSON, MyFirstData.class));

        // 忽略 JSON 串中存在，Java 类中不存在的字段
        JsonMapper jsonMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        try {
            MyFirstData data = jsonMapper.readValue(JSON, MyFirstData.class);
            assertThat(data).extracting(MyFirstData::getIAge).isNull();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MyFirstData data = new MyFirstData();
        data.setIAge(21);

        String result = mapper.writeValueAsString(data);
        JsonAssertions.assertThatJson(result).isObject()
                // 不存在 iAge
                .doesNotContainKey("iAge")
                // 存在 iage
                .containsEntry("iage", 21);
    }

    @SuppressWarnings("SpellCheckingInspection")
    static class MySecondData {
        private Integer iAge;

        public Integer getiAge() {
            return iAge;
        }

        public void setiAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    public void testDeserializeSecondData() {
        validDeserializeToTargetClass(MySecondData.class);
    }

    @Getter
    @Setter
    static class MyThirdData {
        @JsonProperty("iAge")
        private Integer iAge;
    }

    @Test
    public void testDeserializeThirdData() {
        validDeserializeToTargetClass(MyThirdData.class);
    }

    @Getter
    static class MyFourthData {
        private Integer iAge;

        @JsonProperty("iAge")
        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    public void testDeserializeFourthData() {
        validDeserializeToTargetClass(MyFourthData.class);
    }

    @SneakyThrows
    private <T> void validDeserializeToTargetClass(Class<T> clazz) {
        T data = mapper.readValue(JSON, clazz);
        assertThat(data).isNotNull()
                .extracting("iAge")
                .isEqualTo(21);

        String result = mapper.writeValueAsString(data);
        JsonAssertions.assertThatJson(result).isObject().isEqualTo(JSON);
    }
}
