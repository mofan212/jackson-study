package indi.mofan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/6 16:11
 * @link <a href="https://www.baeldung.com/jackson-field-serializable-deserializable-or-not">Jackson – Decide What Fields Get Serialized/Deserialized</a>
 */
public class DecideFieldSerializedAndDeserializedTest implements WithAssertions {

    private static class MyDtoAccessLevel {
        private String stringValue;
        int intValue;
        protected float floatValue;
        public boolean booleanValue;

        // NO setters or getters

        public static MyDtoAccessLevel buildDefault() {
            MyDtoAccessLevel dto = new MyDtoAccessLevel();
            dto.stringValue = "mofan";
            dto.intValue = 212;
            dto.floatValue = 2.12F;
            dto.booleanValue = true;
            return dto;
        }
    }

    @Test
    @SneakyThrows
    public void testPublicField() {
        MyDtoAccessLevel dto = MyDtoAccessLevel.buildDefault();

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(dto);
        // language=JSON
        String expectJson = """
                {
                  "booleanValue": true
                }
                """;
        // 在没有任何 Getter/Setter 的情况下，只序列化 public 字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class MyDtoWithGetter {
        @Getter
        private String stringValue;
        private int intValue;
    }

    @Test
    @SneakyThrows
    public void testPrivateFieldWithGetter() {
        MyDtoWithGetter dto = new MyDtoWithGetter();
        dto.stringValue = "mofan";
        dto.intValue = 212;

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(dto);
        String expectJson = """
                {
                  "stringValue": "mofan"
                }
                """;
        // private 字段有了 Getter，就能够被序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        MyDtoWithGetter value = mapper.readValue(result, MyDtoWithGetter.class);
        // 有了 Getter 后，private 字段会被认为是一个属性，也能成功反序列化
        assertThat(value).extracting(MyDtoWithGetter::getStringValue)
                .isEqualTo("mofan");
    }

    private static class MyDtoWithSetter {
        @Setter
        private Integer intValue;

        public Integer accessIntValue() {
            return this.intValue;
        }
    }

    @Test
    @SneakyThrows
    public void testPrivateFieldWithSetter() {
        // language=JSON
        String targetJson = """
                {
                  "intValue": 212
                }
                """;
        JsonMapper mapper = JsonMapper.builder()
                // 关闭序列化空 Bean 抛出异常的特性
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
        MyDtoWithSetter value = mapper.readValue(targetJson, MyDtoWithSetter.class);
        // 私有字段有 Setter，能够反序列化
        assertThat(value).extracting(MyDtoWithSetter::accessIntValue).isEqualTo(212);

        MyDtoWithSetter dto = new MyDtoWithSetter();
        dto.setIntValue(2);
        // 只有 Setter，只能反序列化，不能序列化
        assertThat(mapper.writeValueAsString(dto)).isEqualTo("{}");
    }

    @Test
    @SneakyThrows
    public void testAllFieldGloballySerializable() {
        JsonMapper mapper = JsonMapper.builder()
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .build();

        MyDtoAccessLevel dto = MyDtoAccessLevel.buildDefault();
        String result = mapper.writeValueAsString(dto);
        String expectJson = """
                {
                  "stringValue": "mofan",
                  "intValue": 212,
                  "floatValue": 2.12,
                  "booleanValue": true
                }
                """;
        // 任何访问修饰符修饰的字段都能被序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class User {
        @Getter
        @Setter
        private String userName;
        private String password;

        @JsonIgnore
        public String getPassword() {
            return password;
        }

        @JsonProperty
        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Test
    @SneakyThrows
    public void testIgnoreFieldSometimes() {
        User user = new User();
        user.setUserName("mofan212");
        user.setPassword("thePassword");

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(user);
        String expectJson = """
                {
                  "userName": "mofan212"
                }
                """;
        // password 不会被序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // language=JSON
        String targetJson = """
                {
                  "userName": "mofan",
                  "password": "thePassword"
                }
                """;
        User value = mapper.readValue(targetJson, User.class);
        assertThat(value).extracting(User::getUserName, User::getPassword)
                .containsExactly("mofan", "thePassword");
    }
}
