package indi.mofan.more;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author mofan
 * @date 2024/1/3 11:14
 */
public class JsonIdentityReferenceTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private static class BeanWithoutIdentityReference {
        private int id;
        private String name;
    }

    /**
     * {@code @JsonIdentityReference} 注解常与 {@code @JsonIdentityInfo} 注解一起使用，
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private static class BeanWithIdentityReference {
        private int id;
        private String name;
    }

    @Test
    @SneakyThrows
    public void test() {
        BeanWithoutIdentityReference bean
                = new BeanWithoutIdentityReference(1, "Bean Without Identity Reference Annotation");
        JsonMapper mapper = JsonMapper.builder().build();
        String jsonString = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 1,
                  "name": "Bean Without Identity Reference Annotation"
                }
                """;
        JsonAssertions.assertThatJson(jsonString).isEqualTo(expectJson);

        BeanWithIdentityReference anotherBean
                = new BeanWithIdentityReference(1, "Bean With Identity Reference Annotation");
        jsonString = mapper.writeValueAsString(anotherBean);
        assertThat(jsonString).isEqualTo("1");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private static class User {
        private Integer id;
        private String name;
        private List<Item> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private static class Item {
        private Integer id;
        private String itemName;
        private User owner;
    }

    @JsonIdentityReference(alwaysAsId = true)
    public static class ItemMixIn {

    }

    @Test
    @SneakyThrows
    public void testCircularDependencies() {
        Item item = new Item();
        item.setId(101);
        item.setItemName("item-name");

        User user = new User();
        user.setId(1);
        user.setName("user-name");
        user.setItems(List.of(item));

        item.setOwner(user);

        JsonMapper mapper = JsonMapper.builder().build();
        String userString = mapper.writeValueAsString(user);
        // language=JSON
        String expectUserString = """
                {
                  "id": 1,
                  "name": "user-name",
                  "items": [
                    {
                      "id": 101,
                      "itemName": "item-name",
                      "owner": 1
                    }
                  ]
                }
                """;
        // 循环依赖项使用指定的字段值代替，这里的 owner 使用 1
        JsonAssertions.assertThatJson(userString).isEqualTo(expectUserString);

        String itemString = mapper.writeValueAsString(item);
        // language=JSON
        String expectItemString = """
                {
                  "id": 101,
                  "itemName": "item-name",
                  "owner": {
                    "id": 1,
                    "name": "user-name",
                    "items": [
                      101
                    ]
                  }
                }
                """;
        // 循环依赖项使用指定的字段值代替，这里的 items 使用 101
        JsonAssertions.assertThatJson(itemString).isEqualTo(expectItemString);

        JsonMapper newMapper = JsonMapper.builder()
                .addMixIn(Item.class, ItemMixIn.class)
                .build();
        userString = newMapper.writeValueAsString(user);
        expectUserString = """
                {
                  "id": 1,
                  "name": "user-name",
                  "items": [
                    101
                  ]
                }
                """;
        // 使用 @JsonIdentityReference 后，只用 id 作为每项
        JsonAssertions.assertThatJson(userString).isEqualTo(expectUserString);

        itemString = newMapper.writeValueAsString(item);
        // 序列化后，只有 101
        assertThat(itemString).isEqualTo("101");
    }
}
