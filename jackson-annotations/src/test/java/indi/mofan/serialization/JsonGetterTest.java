package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/11/30 16:35
 * @link <a href="https://www.baeldung.com/jackson-annotations#2-jsongetter">JsonGetter</a>
 */
public class JsonGetterTest implements WithAssertions {

    @AllArgsConstructor
    static class MyBean {
        public int id;
        private String name;

        @JsonGetter("name")
        public String getTheName() {
            return name;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonGetter() {
        MyBean bean = new MyBean(1, "My bean");

        String result = JsonMapper.builder().build().writeValueAsString(bean);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My bean"
                }
                """;

        // @JsonGetter 是 @JsonProperty 的一种替代，能够让任意方法变为指定值的 Getter
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
