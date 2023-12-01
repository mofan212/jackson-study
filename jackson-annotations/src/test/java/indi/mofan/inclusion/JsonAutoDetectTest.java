package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 18:11
 * @link <a href="https://www.baeldung.com/jackson-annotations#6-jsonautodetect">JsonAutoDetect</a>
 */
public class JsonAutoDetectTest implements WithAssertions {
    @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class PrivateBean {
        private Integer id;
        private String name;
    }

    @Test
    @SneakyThrows
    public void testJsonAutoDetect() {
        PrivateBean bean = new PrivateBean(1, "My bean");
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My bean"
                }
                """;
        // 默认情况下，不能访问私有字段，设置字段可见性为 ANY 后，能够访问私有字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
