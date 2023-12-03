package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * {@code @JsonUnwrapped} 用于指示在序列化或反序列化时，标记的值是被包装还是平铺
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 15:05
 * @link <a href="https://www.baeldung.com/jackson-annotations#3-jsonunwrapped">JsonUnwrapped</a>
 */
public class JsonUnwrappedTest implements WithAssertions {

    @AllArgsConstructor
    private static class UnwrappedUser {
        public int id;

        @JsonUnwrapped
        public Name name;

        @AllArgsConstructor
        public static class Name {
            public String firstName;
            public String lastName;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonUnwrapped() {
        UnwrappedUser.Name name = new UnwrappedUser.Name("John", "Doe");
        UnwrappedUser user = new UnwrappedUser(1, name);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(user);
        String expectJson = """
                {
                  "id": 1,
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """;
        // Name 类型的值在虚拟化后，被平铺了
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
