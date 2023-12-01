package indi.mofan.inclusion;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
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
 * @date 2023/12/1 17:04
 * @link <a href="https://www.baeldung.com/jackson-annotations#3-jsonignoretype">JsonIgnoreType </a>
 */
public class JsonIgnoreTypeTest implements WithAssertions {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class User {
        public Integer id;
        public Name name;

        @JsonIgnoreType
        @AllArgsConstructor
        static class Name {
            public String firstName;
            public String lastName;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonIgnoreType() {
        User.Name name = new User.Name("John", "Doe");
        User user = new User(1, name);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(user);
        String expectJson = """
                {
                  "id": 1
                }
                """;
        // 序列化时，忽略 Name 类型
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // language=JSON
        String json = """
                {
                  "id": 2,
                  "name": {
                    "firstName": "John",
                    "lastName": "Doe"
                  }
                }
                """;
        User bean = mapper.readValue(json, User.class);
        assertThat(bean).extracting(User::getId, User::getName)
                // 反序列化时也会忽略
                .containsExactly(2, null);
    }
}
