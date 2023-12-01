package indi.mofan.deserialization;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 15:30
 * @link <a href="https://www.baeldung.com/jackson-annotations#4-jsonsetter">JsonSetter</a>
 */
public class JsonSetterTest implements WithAssertions {
    //language=JSON
    public static final String JSON = """
            {
              "id": 1,
              "name": "My bean"
            }""";

    @Getter
    static class MyBean {
        public int id;
        private String name;

        @JsonSetter("name")
        public void setTheName(String name) {
            this.name = name;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonSetter() {
        JsonMapper mapper = JsonMapper.builder().build();
        MyBean bean = mapper.readValue(JSON, MyBean.class);
        assertThat(bean).isNotNull()
                .extracting(MyBean::getName)
                .isEqualTo("My bean");
    }
}
