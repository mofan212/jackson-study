package indi.mofan.more;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * 对某个字段只进行反序列化，而不序列化
 *
 * @author mofan
 * @date 2025/6/26 17:06
 */
public class WriteOnlyTest implements WithAssertions {

    ObjectMapper mapper = JsonMapper.builder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    static class MyClass1 {
        private String str;

        @JsonIgnore
        public String getStr() {
            return str;
        }

        @JsonProperty
        public void setStr(String str) {
            this.str = str;
        }
    }

    @Test
    @SneakyThrows
    public void testWriteOnly() {
        String expected = "{\"str\":\"hello\"}";
        MyClass1 obj = mapper.readValue(expected, MyClass1.class);
        assertThat(obj).extracting(MyClass1::getStr).isEqualTo("hello");

        String json = mapper.writeValueAsString(obj);
        assertThat(json).isEqualTo("{}");
    }

    @Getter
    @Setter
    static class MyClass2 {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String str;
    }

    @Test
    @SneakyThrows
    public void testAccessWriteOnly() {
        String expected = "{\"str\":\"hello\"}";
        MyClass2 obj = mapper.readValue(expected, MyClass2.class);
        assertThat(obj).extracting(MyClass2::getStr).isEqualTo("hello");

        String json = mapper.writeValueAsString(obj);
        assertThat(json).isEqualTo("{}");
    }
}
