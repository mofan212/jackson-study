package indi.mofan.deserialization;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 14:18
 * @link <a href="https://www.baeldung.com/jackson-annotations#2-jacksoninject">JacksonInject</a>
 */
public class JacksonInjectTest implements WithAssertions {
    //language=JSON
    public static final String JSON_WITHOUT_ID = """
            {
              "name": "My bean"
            }""";

    public static final String JSON = """
            {
              "id": 2,
              "name": "My bean"
            }""";

    @Getter
    static class BeanWithInject {

        @JacksonInject(value = "inject-id")
        public int id;

        public String name;
    }

    @Test
    @SneakyThrows
    public void testJacksonInject() {
        InjectableValues inject = new InjectableValues.Std()
                // 把 id 值设置为 1
                .addValue("inject-id", 1);
        JsonMapper mapper = JsonMapper.builder().build();
        ObjectReader reader = mapper.reader(inject);

        BeanWithInject bean = reader.readValue(JSON_WITHOUT_ID, BeanWithInject.class);
        assertThat(bean).isNotNull()
                .extracting(BeanWithInject::getId, BeanWithInject::getName)
                // 虽然 JSON 数据里没有 id 信息，但能通过 InjectableValues 设置进去
                .containsExactly(1, "My bean");

        bean = reader.readValue(JSON, BeanWithInject.class);
        assertThat(bean).isNotNull()
                .extracting(BeanWithInject::getId, BeanWithInject::getName)
                // 如果 JSON 数据中有 id 信息，注入的值只作为默认值，仍会被覆盖
                .containsExactly(2, "My bean");
    }
}
