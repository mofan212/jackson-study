package indi.mofan.more;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * 对某个字段只进行序列化，而不反序列化
 *
 * @author mofan
 * @date 2025/6/20 17:19
 */
public class ReadOnlyTest implements WithAssertions {

    @Getter
    @Setter
    static class MyClass1 {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String a;
    }

    @Test
    @SneakyThrows
    public void testAccessReadOnly() {
        ObjectMapper mapper = new JsonMapper();
        MyClass1 obj = new MyClass1();
        obj.a = "a";
        String json = mapper.writeValueAsString(obj);
        String expected = "{\"a\":\"a\"}";
        JsonAssertions.assertThatJson(json).isEqualTo(expected);

        MyClass1 myClass1 = mapper.readValue(json, MyClass1.class);
        assertThat(myClass1.a).isNull();
    }
}
