package indi.mofan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
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

    static class MyFifthData {
        private Integer iAge;

        /**
         * iAge 生成 getIAge，模拟使用 Lombok
         */
        public Integer getIAge() {
            return this.iAge;
        }
    }

    static class MyFifthData2 extends MyFifthData {
        public Integer iage;
    }

    @Test
    @SneakyThrows
    public void testDeserializeFifthData() {
        MyFifthData data = new MyFifthData();
        data.iAge = 21;
        String result = mapper.writeValueAsString(data);
        // language=JSON
        String expectJson = """
                {
                  "iage": 21
                }
                """;
        // 只有 Getter 能够被序列化，但被序列化为 iage
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                // 只有 Getter 也能反序列化，但反序列化时失败，需要类中存在 iage 属性
                .isThrownBy(() -> mapper.readValue(result, MyFifthData.class));

        // 有 public 的 iage 字段，能被反序列化
        MyFifthData2 value = mapper.readValue(result, MyFifthData2.class);
        assertThat(value).extracting(i -> i.iage).isEqualTo(21);
    }

    static class MySixthData {
        private Integer iAge;

        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    static class MySixthDataMixIn {
        private Integer iAge;

        @JsonProperty("iAge")
        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    @SneakyThrows
    public void testDeserializeSixthData() {
        MySixthData data = new MySixthData();
        data.setIAge(21);
        // 序列化失败，可以自定义序列化器或者关闭 `SerializationFeature.FAIL_ON_EMPTY_BEANS` 特性来解决
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> mapper.writeValueAsString(data));

        // language=JSON
        String inputJson = """
                {
                  "iage": 21
                }
                """;
        MySixthData value = mapper.readValue(inputJson, MySixthData.class);
        assertThat(value).extracting(i -> i.iAge).isEqualTo(21);

        JsonMapper jsonMapper = JsonMapper.builder()
                // 使用 MixIn 为 Setter 添加  @JsonProperty 注解
                .addMixIn(MySixthData.class, MySixthDataMixIn.class)
                .build();
        // 依旧不能序列化
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> jsonMapper.writeValueAsString(data));

        inputJson = """
                {
                  "iAge": 21
                }""";
        // 能够将指定的字段反序列化
        value = jsonMapper.readValue(inputJson, MySixthData.class);
        assertThat(value).extracting(i -> i.iAge).isEqualTo(21);
    }

    /**
     * 自行实现 Getter/Setter，模拟使用 Lombok
     */
    static class MySeventhData {
        private Integer iAge;

        public Integer getIAge() {
            return iAge;
        }

        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    @SneakyThrows
    public void testDeserializeSeventhData() {
        MySeventhData data = new MySeventhData();
        data.setIAge(21);

        String result = mapper.writeValueAsString(data);
        // language=JSON
        String expectJson = """
                {
                  "iage": 21
                }
                """;
        // 有 Getter，能被序列化，但是序列化成 iage
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        MySeventhData value = mapper.readValue(result, MySeventhData.class);
        // 反序列化也是没有问题的
        assertThat(value).extracting(MySeventhData::getIAge).isEqualTo(21);

        // language=JSON
        String inputJson = """
                {
                  "iAge": 21
                }
                """;
        // 使用与字段名称相同的 JSON 反序列化成功
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(inputJson, MySeventhData.class));
    }

    /**
     * 与 {@link MySeventhData} 基本一样，但在 Setter 上添加 @JsonProperty 注解
     */
    static class MyEighthData {
        private Integer iAge;

        public Integer getIAge() {
            return iAge;
        }

        @JsonProperty("iAge")
        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    @SneakyThrows
    public void testDeserializeEightData() {
        MyEighthData data = new MyEighthData();
        data.setIAge(21);

        String result = mapper.writeValueAsString(data);
        // language=JSON
        String expectJson = """
                {
                  "iAge": 21
                }
                """;
        // 能序列化，但是序列化为 iAge，注解并没有打在 Getter 上
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        MyEighthData value = mapper.readValue(result, MyEighthData.class);
        // 成功反序列化
        assertThat(value).extracting(MyEighthData::getIAge).isEqualTo(21);
    }

    /**
     * 只有 Setter，且使用了 @JsonProperty 注解
     */
    static class MyNinthData {
        private Integer iAge;

        @JsonProperty("iAge")
        public void setIAge(Integer iAge) {
            this.iAge = iAge;
        }
    }

    @Test
    @SneakyThrows
    public void testSerializeNinthData() {
        MyNinthData data = new MyNinthData();
        data.setIAge(21);

        // 没有 Getter 序列化失败
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> mapper.writeValueAsString(data));

        // language=JSON
        String inputJson = """
                {
                  "iAge": 21
                }
                """;
        MyNinthData value = mapper.readValue(inputJson, MyNinthData.class);
        // 反序列化成功
        assertThat(value).extracting(i -> i.iAge).isEqualTo(21);
    }

    /**
     * 只有 Getter，且使用了 @JsonProperty 注解
     */
    static class MyTenthData {
        private Integer iAge;

        @JsonProperty("iAge")
        public Integer getIAge() {
            return iAge;
        }
    }

    @Test
    @SneakyThrows
    public void testSerializeTenthData() {
        MyTenthData data = new MyTenthData();
        data.iAge = 21;

        String result = mapper.writeValueAsString(data);
        // language=JSON
        String expectJson = """
                {
                  "iAge": 21
                }
                """;
        // 能被序列化，且按照 @JsonProperty 字段的字段进行序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 用序列化得到的 JSON 进行反序列化会失败
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(result, MyTenthData.class));

        // language=JSON
        String inputJson = """
                {
                  "iage": 21
                }
                """;
        // 使用 iage 反序列化也会失败
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(inputJson, MyTenthData.class));
    }
}
