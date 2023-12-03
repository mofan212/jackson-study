package indi.mofan.disable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * 通过禁用 {@code MapperFeature.USE_ANNOTATIONS} 即可实现禁用所有 Jackson 注解
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 16:34
 * @link <a href="https://www.baeldung.com/jackson-annotations#disable-jackson-annotation">Disable Jackson Annotation</a>
 */
public class DisableAllAnnotationsTest implements WithAssertions {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"name", "id"})
    @AllArgsConstructor
    private static class MyBean {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testDisableAllAnnotations() {
        MyBean bean = new MyBean(1, null);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "id": 1
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 禁用所有注解
        mapper = JsonMapper.builder()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .build();
        result = mapper.writeValueAsString(bean);
        expectJson = """
                {
                  "id": 1,
                  "name":null
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
