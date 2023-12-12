package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.MapperFeature;
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
 * @date 2023/12/12 14:37
 * @link <a href="https://www.baeldung.com/jackson-json-view-annotation">Jackson JSON Views</a>
 */
public class JsonViewTest implements WithAssertions {
    private static class Views {
        private static class Public {
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        public int id;

        @JsonView(Views.Public.class)
        public String name;
    }
    
    @Test
    @SneakyThrows
    public void testSerializeByView() {
        User user = new User(1, "John");

        JsonMapper mapper = JsonMapper.builder()
                // 禁用默认视图，未被 @JsonView 注解标记的字段用不参与序列化
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();

        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(user);
        String expectJson = """
                {
                  "name": "John"
                }
                """;
        // 只序列化 Views.Public.class 标记的字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        mapper = JsonMapper.builder().build();
        result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(user);
        //language=JSON
        expectJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        // 未禁用 DEFAULT_VIEW_INCLUSION 时，未被 @JsonView 标记的字段默认参与序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
