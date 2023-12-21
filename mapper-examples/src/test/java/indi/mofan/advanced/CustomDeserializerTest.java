package indi.mofan.advanced;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2023/12/20 11:18
 * @link <a href="https://www.baeldung.com/jackson-deserialization">Getting Started with Custom Deserialization in Jackson</a>
 */
public class CustomDeserializerTest implements WithAssertions {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        public int id;
        public String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Item {
        public int id;
        public String itemName;
        public User owner;
    }

    @Test
    @SneakyThrows
    public void testStandardDeserialization() {
        // language=JSON
        String inputJson = """
                {
                    "id": 1,
                    "itemName": "theItem",
                    "owner": {
                        "id": 2,
                        "name": "theUser"
                    }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        Item item = mapper.readValue(inputJson, Item.class);
        assertThat(item).extracting(Item::getId, Item::getItemName, i -> i.getOwner().getId(), i -> i.getOwner().getName())
                .containsExactly(1, "theItem", 2, "theUser");
    }

    private static class ItemDeserializer extends StdDeserializer<Item> {

        @Serial
        private static final long serialVersionUID = -6835593961657959163L;

        public ItemDeserializer() {
            this(null);
        }

        public ItemDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Item deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            int id = (Integer) node.get("id").numberValue();
            String itemName = node.get("itemName").asText();
            int userId = (Integer) node.get("createdBy").numberValue();

            return new Item(id, itemName, new User(userId, null));
        }
    }

    @Test
    @SneakyThrows
    public void testCustomDeserializerOnObjectMapper() {
        // language=JSON
        String inputJson = """
                {
                    "id": 1,
                    "itemName": "theItem",
                    "createdBy": 2
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(inputJson, Item.class));

        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Item.class, new ItemDeserializer()))
                .build();
        Item item = jsonMapper.readValue(inputJson, Item.class);
        assertThat(item).extracting(Item::getId, Item::getItemName, i -> i.getOwner().getId())
                .containsExactly(1, "theItem", 2);
    }

    @JsonDeserialize(using = ItemDeserializer.class)
    private static class ItemMixIn {
    }

    @Test
    @SneakyThrows
    public void testCustomDeserializerOnClass() {
        // language=JSON
        String inputJson = """
                {
                    "id": 1,
                    "itemName": "theItem",
                    "createdBy": 2
                }
                """;
        JsonMapper mapper = JsonMapper.builder()
                // 不修改 Item 类，采用 MixIn 实现在类上添加 `@JsonDeserialize` 注解
                .addMixIn(Item.class, ItemMixIn.class)
                .build();
        Item item = mapper.readValue(inputJson, Item.class);
        assertThat(item).extracting(Item::getId, Item::getItemName, i -> i.getOwner().getId())
                .containsExactly(1, "theItem", 2);
    }

    private static class Wrapper<T> {

        T value;

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemWithWrapper {
        private int id;
        private String itemName;
        private Wrapper<User> owner;
    }

    private static class WrapperDeserializer extends JsonDeserializer<Wrapper<?>> implements ContextualDeserializer {

        private JavaType type;

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            // 获取 Wrapper 内部的具体类型
            this.type = property.getType().containedType(0);
            return this;
        }

        @Override
        public Wrapper<?> deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
            Wrapper<?> wrapper = new Wrapper<>();
            wrapper.setValue(context.readValue(jsonParser, type));
            return wrapper;
        }
    }

    @Test
    @SneakyThrows
    public void testCustomDeserializerForGenericType() {
        // language=JSON
        String inputJson = """
                {
                    "id": 1,
                    "itemName": "theItem",
                    "owner": {
                        "id": 2,
                        "name": "theUser"
                    }
                }
                """;
        JsonMapper mapper = JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Wrapper.class, new WrapperDeserializer()))
                .build();
        ItemWithWrapper item = mapper.readValue(inputJson, ItemWithWrapper.class);
        assertThat(item).extracting(ItemWithWrapper::getId, ItemWithWrapper::getItemName, i -> i.getOwner().getValue().getId())
                .containsExactly(1, "theItem", 2);
    }
}
