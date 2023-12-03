package indi.mofan;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import indi.mofan.serializer.custom.ItemDeserializer;
import indi.mofan.serializer.custom.ItemSerializer;
import indi.mofan.serializer.pojo.Item;
import indi.mofan.serializer.pojo.User;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/3 22:31
 * @link <a href="https://www.baeldung.com/jackson-object-mapper-tutorial#2-creating-custom-serializer-or-deserializer">Creating Custom Serializer or Deserializer </a>
 * @link <a href="https://www.baeldung.com/jackson-custom-serialization">Jackson – Custom Serializer</a>
 */
public class CustomSerializerTest implements WithAssertions {

    // language=JSON
    private static final String DESERIALIZE_RESULT = """
            {
              "id": 1,
              "itemName": "theItem",
              "owner": 2
            }
            """;

    @Test
    @SneakyThrows
    public void testCustomItemSerializer() {
        Item myItem = new Item(1, "theItem", new User(2, "theUser"));
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(myItem);
        // language=JSON
        String expectJson = """
                {
                  "id": 1,
                  "itemName": "theItem",
                  "owner": {
                    "id": 2,
                    "name": "theUser"
                  }
                }
                """;
        // 未添加自定义序列化器前，owner 字段是个对象
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        SimpleModule module = new SimpleModule();
        module.addSerializer(Item.class, new ItemSerializer());
        mapper = JsonMapper.builder()
                .addModule(module)
                .build();

        String serialized = mapper.writeValueAsString(myItem);
        // 添加序列化器后，按自定义序列化器的逻辑处理
        // 添加序列化器的方式除了在 ObjectMapper 上处理外，还可以使用 @JsonSerialize 注解
        JsonAssertions.assertThatJson(serialized).isEqualTo(DESERIALIZE_RESULT);
    }

    @Test
    @SneakyThrows
    public void testCustomItemDeserializer() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Item.class, new ItemDeserializer());
        JsonMapper mapper = JsonMapper.builder()
                .addModule(module)
                .build();

        Item item = mapper.readValue(DESERIALIZE_RESULT, Item.class);
        assertThat(item).isNotNull()
                .extracting(
                        Item::getId, Item::getItemName,
                        i -> i.getOwner().getId(),
                        i -> i.getOwner().getName())
                .containsExactly(1, "theItem", 2, null);
    }
}
