package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 16:48
 * @link <a href="https://www.baeldung.com/jackson-annotations#2-jsonignore">JsonIgnore</a>
 */
public class JsonIgnoreTest implements WithAssertions {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class BeanWithIgnore {
        @JsonIgnore
        public Integer id;

        public String name;
    }

    //language=JSON
    public static final String JSON = """
            {
              "id": "1",
              "name": "My bean"
            }
            """;

    @Test
    @SneakyThrows
    public void testJsonIgnore() {
        BeanWithIgnore bean = new BeanWithIgnore(2, "bean");
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "name": "bean"
                }
                """;
        // 序列化时，忽略 id
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        BeanWithIgnore resultBean = mapper.readValue(JSON, BeanWithIgnore.class);
        assertThat(resultBean).extracting(BeanWithIgnore::getId, BeanWithIgnore::getName)
                // 反序列化时，也忽略 id
                .containsExactly(null, "My bean");
    }
}
