package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 18:07
 * @link <a href="https://www.baeldung.com/jackson-annotations#5-jsonincludeproperties">JsonIncludeProperties</a>
 */
public class JsonIncludePropertiesTest implements WithAssertions {

    @AllArgsConstructor
    @JsonIncludeProperties({"name"})
    private static class BeanWithInclude {
        public Integer id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonIncludeProperties() {
        BeanWithInclude bean = new BeanWithInclude(1, "My bean");
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "name": "My bean"
                }
                """;
        // 序列化的数据中只包含显式声明的字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
