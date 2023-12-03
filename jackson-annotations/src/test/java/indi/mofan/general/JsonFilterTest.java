package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * {@code @JsonFilter} 用于指定序列化期间所用的过滤器
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 15:52
 * @link <a href="https://www.baeldung.com/jackson-annotations#7-jsonfilter">JsonFilter</a>
 */
public class JsonFilterTest implements WithAssertions {

    @JsonFilter("myFilter")
    @AllArgsConstructor
    private static class BeanWithFilter {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonFilter() {
        BeanWithFilter bean = new BeanWithFilter(1, "My bean");

        SimpleFilterProvider provider = new SimpleFilterProvider();
        FilterProvider filters = provider.addFilter(
                "myFilter",
                // 过滤所有字段，除了 name 字段
                SimpleBeanPropertyFilter.filterOutAllExcept("name")
        );

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writer(filters).writeValueAsString(bean);
        String expectJson = """
                {
                  "name": "My bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
