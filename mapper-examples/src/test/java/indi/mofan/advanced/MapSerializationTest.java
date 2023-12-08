package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mofan
 * @date 2023/12/8 10:24
 * @link <a href="https://www.baeldung.com/jackson-map">Map Serialization and Deserialization with Jackson</a>
 */
public class MapSerializationTest implements WithAssertions {
    @Test
    @SneakyThrows
    public void testSerializeStringKeyStringValue() {

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(map);
        String expectJson = """
                {
                  "key": "value"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        var value = mapper.readValue(result, new TypeReference<Map<String, String>>() {
        });
        assertThat(value).isEqualTo(map);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class MyPair {

        private String first;
        private String second;

        public MyPair(String both) {
            String[] pairs = both.split("and");
            this.first = pairs[0].trim();
            this.second = pairs[1].trim();
        }

        @Override
        @JsonValue
        public String toString() {
            return first + " and " + second;
        }
    }

    private static class MyPairSerializer extends JsonSerializer<MyPair> {

        private static final JsonMapper MAPPER = JsonMapper.builder().build();

        @Override
        public void serialize(MyPair value,
                              JsonGenerator gen,
                              SerializerProvider serializers)
                throws IOException {

            StringWriter writer = new StringWriter();
            MAPPER.writeValue(writer, value);
            gen.writeFieldName(writer.toString());
        }
    }

    @JsonSerialize(keyUsing = MyPairSerializer.class)
    Map<MyPair, String> objectKeyStringValueMap;

    @Test
    @SneakyThrows
    public void testSerializeObjectKeyStringValue() {
        objectKeyStringValueMap = new HashMap<>();
        MyPair key = new MyPair("Abbott", "Costello");
        objectKeyStringValueMap.put(key, "Comedy");

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(objectKeyStringValueMap);
        // language=JSON
        String expectJson = """
                {
                  "Abbott and Costello": "Comedy"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Map<MyPair, String> value = mapper.readValue(result, new TypeReference<>() {
        });
        // 必须提供 String 类型的构造器
        assertThat(value).isNotEmpty()
                .containsEntry(new MyPair("Abbott", "Costello"), "Comedy");
    }

    @Getter
    @Setter
    private static class ClassWithAMap {

        @JsonProperty("map")
        @JsonDeserialize(keyUsing = MyPairDeserializer.class)
        private Map<MyPair, String> map;

        @JsonCreator
        public ClassWithAMap(Map<MyPair, String> map) {
            this.map = map;
        }
    }

    private static class MyPairDeserializer extends KeyDeserializer {

        @Override
        public MyPair deserializeKey(String key, DeserializationContext context) {
            return new MyPair(key);
        }
    }

    @Test
    @SneakyThrows
    public void testDeserializeObjectKeyStringValue() {
        String jsonInput = "{\"Abbott and Costello\":\"Comedy\"}";

        JsonMapper mapper = JsonMapper.builder().build();
        ClassWithAMap classWithMap = mapper.readValue(jsonInput, ClassWithAMap.class);
        assertThat(classWithMap.getMap())
                .hasSize(1)
                .containsEntry(new MyPair("Abbott", "Costello"), "Comedy");
    }

    @JsonSerialize(keyUsing = MapSerializer.class)
    Map<MyPair, MyPair> objectKeyObjectValueMap;

    @JsonSerialize(keyUsing = MyPairSerializer.class)
    MyPair mapKey;

    @JsonSerialize(keyUsing = MyPairSerializer.class)
    MyPair mapValue;

    @Test
    @SneakyThrows
    public void testSerializeObjectKeyObjectValue() {
        mapKey = new MyPair("Abbott", "Costello");
        mapValue = new MyPair("Comedy", "1940s");
        objectKeyObjectValueMap = new HashMap<>();
        objectKeyObjectValueMap.put(mapKey, mapValue);

        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(objectKeyObjectValueMap);
        // language=JSON
        String expectJson = """
                {
                  "Abbott and Costello": "Comedy and 1940s"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        var value = mapper.readValue(result, new TypeReference<HashMap<MyPair, MyPair>>() {
        });
        assertThat(value).hasSize(1)
                .containsEntry(new MyPair("Abbott", "Costello"), new MyPair("Comedy", "1940s"));
    }

    private static class Fruit {
        public String variety;

        @JsonKey
        public String name;

        public Fruit(String variety, String name) {
            this.variety = variety;
            this.name = name;
        }

        @JsonValue
        public String getFullName() {
            return this.variety + " " + this.name;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonKeyAnnotation() {
        final Fruit fruit1 = new Fruit("Alphonso", "Mango");
        final Fruit fruit2 = new Fruit("Black", "Grapes");
        JsonMapper mapper = JsonMapper.builder().build();

        // 序列化出的值不是 JSON 串
        String fruit1Result = mapper.writeValueAsString(fruit1);
        assertThat(fruit1Result).isEqualTo("\"Alphonso Mango\"");
        String fruit2Result = mapper.writeValueAsString(fruit2);
        assertThat(fruit2Result).isEqualTo("\"Black Grapes\"");

        Map<Fruit, String> fruitKeyMap = new LinkedHashMap<>();
        fruitKeyMap.put(fruit1, "Hagrid");
        fruitKeyMap.put(fruit2, "Hercules");
        String fruitKeyMapResult = mapper.writeValueAsString(fruitKeyMap);
        // language=JSON
        String expectFruitKeyMapJson = """
                {
                  "Mango": "Hagrid",
                  "Grapes": "Hercules"
                }
                """;
        // Fruit 作为 Key 时，使用被 @JsonKey 注解标记的字段值作为 Json 的 Key
        JsonAssertions.assertThatJson(fruitKeyMapResult).isEqualTo(expectFruitKeyMapJson);

        Map<String, Fruit> fruitValueMap = new LinkedHashMap<>();
        fruitValueMap.put("Hagrid", fruit1);
        fruitValueMap.put("Hercules", fruit2);
        String fruitValueMapResult = mapper.writeValueAsString(fruitValueMap);
        // language=JSON
        String expectFruitValueMapJson = """
                {
                  "Hagrid": "Alphonso Mango",
                  "Hercules": "Black Grapes"
                }
                """;
        JsonAssertions.assertThatJson(fruitValueMapResult).isEqualTo(expectFruitValueMapJson);
    }
}
