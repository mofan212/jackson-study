package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonView;
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
 * {@code @JsonView} 用于指定在序列化或反序列化时哪些字段会被包含在视图中
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 15:11
 * @link <a href="https://www.baeldung.com/jackson-annotations#4-jsonview">JsonView</a>
 */
public class JsonViewTest implements WithAssertions {
    private static class Views {
        public static class Public {
        }

        public static class Internal extends Public {
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Item {
        @JsonView(Views.Public.class)
        public Integer id;

        @JsonView(Views.Public.class)
        public String itemName;

        @JsonView(Views.Internal.class)
        public String ownerName;
    }

    @Test
    @SneakyThrows
    public void testJsonView() {
        Item item = new Item(2, "book", "John");
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(item);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // language=JSON
        String inputJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "ownerName": "John"
                }
                """;
        Item value = mapper.readerWithView(Views.Internal.class)
                .readValue(inputJson, Item.class);
        // Internal 继承了 Public，因此指定为 Internal 时，也会包含 Public 的数据
        assertThat(value).extracting(Item::getId, Item::getItemName, Item::getOwnerName)
                .containsExactly(2, "book", "John");
    }
}
