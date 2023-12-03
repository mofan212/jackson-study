package indi.mofan.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/3 16:23
 * @link <a href="https://www.baeldung.com/jackson-annotations#jackson-mixin-annotations">Jackson MixIn Annotations</a>
 */
public class MixInTest implements WithAssertions {

    @AllArgsConstructor
    private static class Item {
        public int id;
        public String itemName;
        public User owner;
    }

    private static class User {
    }

    @JsonIgnoreType
    private static class MyMixInForIgnoreType {
    }

    @Test
    @SneakyThrows
    public void testMixIn() {
        Item item = new Item(1, "book", null);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(item);
        String expectJson = """
                {
                  "id": 1,
                  "itemName": "book",
                  "owner": null
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        mapper = JsonMapper.builder()
                /*
                 * MyMixInForIgnoreType 相当于等价于 User，作用在
                 * MyMixInForIgnoreType 上的注解也会作用在 User 上
                 */
                .addMixIn(User.class, MyMixInForIgnoreType.class)
                .build();
        result = mapper.writeValueAsString(item);
        // language=JSON
        expectJson = """
                {
                  "id": 1,
                  "itemName": "book"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
