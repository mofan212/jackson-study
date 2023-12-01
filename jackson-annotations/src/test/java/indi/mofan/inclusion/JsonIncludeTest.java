package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 17:53
 * @link <a href="https://www.baeldung.com/jackson-annotations#4-jsoninclude">JsonInclude</a>
 */
public class JsonIncludeTest implements WithAssertions {

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class MyBean {
        public Integer id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonInclude() {
        MyBean bean = new MyBean(1, null);
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "id": 1
                }
                """;
        /*
         * @JsonInclude(JsonInclude.Include.NON_NULL) 表示只包含非 null
         * 的数据，此外还可以排除 empty、default value 等数据
         */
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

}
