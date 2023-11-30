package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 10:51
 * @link <a href="https://www.baeldung.com/jackson-annotations#5-jsonvalue">JsonRootName</a>
 */
public class JsonRootNameTest implements WithAssertions {

    private final static JsonMapper SIMPLE_MAPPER = JsonMapper.builder().build();
    private final static JsonMapper WRAP_ROOT_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.WRAP_ROOT_VALUE)
            .build();

    @AllArgsConstructor
    static class User {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testWrapRoot() {
        User user = new User(1, "John");
        String result = SIMPLE_MAPPER.writeValueAsString(user);
        String expectJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        result = WRAP_ROOT_MAPPER.writeValueAsString(user);
        //language=JSON
        expectJson = """
                {
                  "User": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        // 启用 WRAP_ROOT_VALUE 后，才会再外面包一层，默认是类名
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @AllArgsConstructor
    @JsonRootName(value = "user")
    static class UserWithRoot {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonRootName() {
        UserWithRoot user = new UserWithRoot(1, "John");
        String result = WRAP_ROOT_MAPPER.writeValueAsString(user);
        String expectJson = """
                {
                  "user": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        // 使用 @JsonRootName 注解后，使用指定的名称在最外层包裹
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        result = SIMPLE_MAPPER.writeValueAsString(user);
        //language=JSON
        String unwrapJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        // 就算使用 @JsonRootName 注解，但没有开启 WRAP_ROOT_VALUE，最终是无用的
        JsonAssertions.assertThatJson(result).isNotEqualTo(expectJson)
                .isEqualTo(unwrapJson);
    }
}
