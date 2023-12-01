package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * @date 2023/12/1 16:22
 * @link <a href="https://www.baeldung.com/jackson-annotations#1-jsonignoreproperties">JsonIgnoreProperties</a>
 */
public class JsonIgnorePropertiesTest implements WithAssertions {

    @AllArgsConstructor
    @JsonIgnoreProperties({"id"})
    static class BeanWithIgnore {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonIgnoreProperties() {
        BeanWithIgnore bean = new BeanWithIgnore(1, "My bean");
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "name": "My bean"
                }""";
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(value = "id", ignoreUnknown = true)
    static class AnotherBeanWithIgnore {
        public Integer id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testIgnoreUnknownPropertiesInJsonInput() {
        String json = """
                {
                  "id": 2,
                  "name": "My another bean",
                  "level": 6
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        /*
         * 反序列化时，忽略 id 字段，还忽略任何在 JSON 无法识别的字段
         * ignoreUnknown 属性只针对反序列化，对序列化没影响
         */
        AnotherBeanWithIgnore bean = mapper.readValue(json, AnotherBeanWithIgnore.class);
        assertThat(bean).extracting(AnotherBeanWithIgnore::getId, AnotherBeanWithIgnore::getName)
                .containsExactly(null, "My another bean");
    }
}
