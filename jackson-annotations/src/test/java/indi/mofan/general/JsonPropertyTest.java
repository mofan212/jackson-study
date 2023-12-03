package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * {@code @JsonProperty} 注解用于设置序列化或反序列化时 JSON 中的字段名称
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 14:39
 * @link <a href="https://www.baeldung.com/jackson-annotations#1-jsonproperty">JsonProperty</a>
 */
public class JsonPropertyTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MyBean {
        public int id;
        private String name;

        @JsonProperty("name")
        public void setTheName(String name) {
            this.name = name;
        }

        @JsonProperty("name")
        public String getTheName() {
            return name;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonProperty() {
        MyBean bean = new MyBean(1, "My bean");
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        MyBean value = mapper.readValue(result, MyBean.class);
        assertThat(value).extracting(MyBean::getTheName).isEqualTo("My bean");
    }
}
