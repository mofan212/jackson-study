package indi.mofan.deserialization;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 16:11
 * @link <a href="https://www.baeldung.com/jackson-annotations#6-jsonalias">JsonAlias</a>
 */
public class JsonAliasTest implements WithAssertions {
    //language=JSON
    public static final String JSON = """
            {
              "fName": "John",
              "lastName": "Green"
            }""";

    @Getter
    static class AliasBean {

        @JsonAlias({ "fName", "f_name" })
        private String firstName;
        private String lastName;
    }

    @Test
    @SneakyThrows
    public void testJsonAlias() {
        JsonMapper mapper = JsonMapper.builder().build();
        AliasBean bean = mapper.readValue(JSON, AliasBean.class);
        // @JsonAlias 注解用于指定反序列时 JSON 数据的字段别名
        assertThat(bean.getFirstName()).isEqualTo("John");
    }
}
