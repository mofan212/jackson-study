package indi.mofan;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/21 19:26
 * @link <a href="https://www.baeldung.com/jackson-bidirectional-relationships-and-infinite-recursion">Jackson – Bidirectional Relationships</a>
 */
public class BidirectionalRelationshipsTest implements WithAssertions {
    @Getter
    @Setter
    @NoArgsConstructor
    private static class User {
        private int id;
        private String name;
        private List<Item> userItems;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
            this.userItems = new ArrayList<>();
        }

        public void addItem(Item item) {
            if (this.userItems == null) {
                this.userItems = new ArrayList<>();
            }
            this.userItems.add(item);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Item {
        private int id;
        private String itemName;
        private User owner;
    }

    @Test
    public void testInfiniteRecursion() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.writeValueAsString(item));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UserWithReference {
        private int id;
        private String name;

        @JsonManagedReference
        private List<ItemWithReference> userItems;

        public UserWithReference(int id, String name) {
            this.id = id;
            this.name = name;
            this.userItems = new ArrayList<>();
        }

        public void addItem(ItemWithReference item) {
            if (this.userItems == null) {
                this.userItems = new ArrayList<>();
            }
            this.userItems.add(item);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ItemWithReference {
        private int id;
        private String itemName;

        @JsonBackReference
        private UserWithReference owner;
    }

    @Test
    @SneakyThrows
    public void testReferenceAnnotation() {
        final UserWithReference user = new UserWithReference(1, "John");
        final ItemWithReference item = new ItemWithReference(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder().build();
        String itemJson = mapper.writeValueAsString(item);
        // language=JSON
        String expectItemJson = """
                {
                  "id": 2,
                  "itemName": "book"
                }
                """;
        // 被 @JsonBackReference 标记的内容会被省略，这个注解不能用于集合
        JsonAssertions.assertThatJson(itemJson).isEqualTo(expectItemJson);

        String userJson = mapper.writeValueAsString(user);
        // language=JSON
        String expectUserJson = """
                {
                  "id": 1,
                  "name": "John",
                  "userItems": [
                    {
                      "id": 2,
                      "itemName": "book"
                    }
                  ]
                }
                """;
        // 被 @JsonManagedReference 标记的内容会被序列化
        JsonAssertions.assertThatJson(userJson).isEqualTo(expectUserJson);
    }

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    private static class UserWithJsonIdentity {
    }

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    private static class ItemWithJsonIdentity {
    }

    @Test
    @SneakyThrows
    public void testJsonIdentityInfo() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder()
                // 使用 MixIn 实现
                .addMixIn(Item.class, ItemWithJsonIdentity.class)
                .addMixIn(User.class, UserWithJsonIdentity.class)
                .build();

        String result = mapper.writeValueAsString(item);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John",
                    "userItems": [
                      2
                    ]
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 反序列化
        Item value = mapper.readValue(result, Item.class);
        assertThat(value).extracting(Item::getId, Item::getItemName, i -> i.getOwner().getName())
                .containsExactly(2, "book", "John");
    }

    private static class UserWithIgnore {
        @JsonIgnore
        public List<Item> userItems;
    }

    @Test
    @SneakyThrows
    public void testJsonIgnore() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(User.class, UserWithIgnore.class)
                .build();

        String result = mapper.writeValueAsString(item);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class Views {
        public static class Public {
        }

        public static class Internal extends Public {
        }
    }

    private static class UserWithView {
        @JsonView(Views.Public.class)
        public int id;

        @JsonView(Views.Public.class)
        public String name;

        @JsonView(Views.Internal.class)
        public List<Item> userItems;
    }

    private static class ItemWithView {
        @JsonView(Views.Public.class)
        public int id;

        @JsonView(Views.Public.class)
        public String itemName;

        @JsonView(Views.Public.class)
        public User owner;
    }

    @Test
    @SneakyThrows
    public void testJsonView() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(User.class, UserWithView.class)
                .addMixIn(Item.class, ItemWithView.class)
                .build();

        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(item);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 所有字段都会包含，循环引用，抛出异常
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.writerWithView(Views.Internal.class)
                        .writeValueAsString(item));
    }

    private static class CustomListSerializer extends StdSerializer<List<Item>> {
        @Serial
        private static final long serialVersionUID = 7006569763021232700L;

        public CustomListSerializer() {
            this(null);
        }

        public CustomListSerializer(Class<List<Item>> t) {
            super(t);
        }

        @Override
        public void serialize(List<Item> items, JsonGenerator generator, SerializerProvider provider)
                throws IOException {
            List<Integer> ids = new ArrayList<>();
            for (Item item : items) {
                // 序列化时只写入 id
                ids.add(item.id);
            }
            generator.writeObject(ids);
        }
    }

    private static class CustomListDeserializer extends StdDeserializer<List<Item>> {
        @Serial
        private static final long serialVersionUID = -9701581462961461L;

        public CustomListDeserializer() {
            this(null);
        }

        public CustomListDeserializer(Class<List<Item>> vc) {
            super(vc);
        }

        @Override
        public List<Item> deserialize(JsonParser jsonparser, DeserializationContext context) {
            // 反序列化成空列表
            return new ArrayList<>();
        }
    }

    private static class UserWithCustomSerializer {
        @JsonSerialize(using = CustomListSerializer.class)
        @JsonDeserialize(using = CustomListDeserializer.class)
        public List<Item> userItems;
    }

    @Test
    @SneakyThrows
    public void testCustomSerializer() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(User.class, UserWithCustomSerializer.class)
                .build();

        String result = mapper.writeValueAsString(item);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John",
                    "userItems": [
                      2
                    ]
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Item value = mapper.readValue(result, Item.class);
        assertThat(value).extracting(i -> i.getOwner().getUserItems())
                .asList()
                .isNotNull()
                .isEmpty();
    }

}
