package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
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

/**
 * @author mofan
 * @date 2023/12/10 16:58
 * @link <a href="https://www.baeldung.com/jackson-serialize-enums">How To Serialize and Deserialize Enums with Jackson</a>
 */
public class SerializeEnumsTest implements WithAssertions {

    @AllArgsConstructor
    private enum DistanceEnumSimple {
        KILOMETER("km", 1000),
        MILE("miles", 1609.34),

        // --snip--
        ;

        @Getter
        @Setter
        private String unit;
        @Getter
        private final double meters;
    }

    @Test
    @SneakyThrows
    public void testDefaultEnumRepresentation() {
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(DistanceEnumSimple.MILE);
        // 默认情况下，直接使用对应枚举的 name 作为序列化结果
        assertThat(result).isEqualTo("\"MILE\"");
    }

    @AllArgsConstructor
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private enum DistanceEnumWithJsonFormat {
        MILE("miles", 1609.34),

        // --snip--
        ;

        @Getter
        @Setter
        private String unit;
        @Getter
        private final double meters;
    }

    @Test
    @SneakyThrows
    public void testAsJsonObject() {
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(DistanceEnumWithJsonFormat.MILE);
        String expectJson = """
                {
                  "unit": "miles",
                  "meters": 1609.34
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }


    @AllArgsConstructor
    private enum DistanceEnumWithValue {
        MILE("miles", 1609.34),

        // --snip--
        ;

        @Setter
        @Getter
        private String unit;

        @Getter(onMethod_ = {@JsonValue})
        private final double meters;
    }

    @Test
    @SneakyThrows
    public void testEnumAndJsonValueAnnotation() {
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(DistanceEnumWithValue.MILE);
        assertThat(result).isEqualTo("1609.34");
    }

    private static class DistanceSerializer extends StdSerializer<DistanceEnumWithCustomSerializer> {
        @Serial
        private static final long serialVersionUID = -5717950039742855142L;

        public DistanceSerializer() {
            super(DistanceEnumWithCustomSerializer.class);
        }

        public DistanceSerializer(Class t) {
            super(t);
        }

        public void serialize(DistanceEnumWithCustomSerializer distance,
                              JsonGenerator generator,
                              SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeFieldName("name");
            generator.writeString(distance.name());
            generator.writeFieldName("unit");
            generator.writeString(distance.getUnit());
            generator.writeFieldName("meters");
            generator.writeNumber(distance.getMeters());
            generator.writeEndObject();
        }
    }

    @AllArgsConstructor
    @JsonSerialize(using = DistanceSerializer.class)
    private enum DistanceEnumWithCustomSerializer {
        MILE("miles", 1609.34),

        // --snip--
        ;

        @Setter
        @Getter
        private String unit;
        @Getter
        private final double meters;
    }

    @Test
    @SneakyThrows
    public void testCustomSerializer() {
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(DistanceEnumWithCustomSerializer.MILE);
        String expectJson = """
                {
                  "name": "MILE",
                  "unit": "miles",
                  "meters": 1609.34
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    // DeSerialization

    @Getter
    @Setter
    @NoArgsConstructor
    private static class City {
        private DistanceEnumSimple distance;
    }

    @Test
    @SneakyThrows
    public void testDefaultDeserialization() {
        JsonMapper mapper = JsonMapper.builder().build();
        // language=JSON
        String inputJson = """
                {
                  "distance": "KILOMETER"
                }
                """;
        // 默认情况下，使用枚举项的 name
        City value = mapper.readValue(inputJson, City.class);
        assertThat(value.getDistance()).isEqualTo(DistanceEnumSimple.KILOMETER);

        // language=JSON
        String caseInsensitiveInputJson = """
                {
                  "distance": "KiLoMeTeR"
                }
                """;
        mapper = JsonMapper.builder()
                // 开启忽略枚举项 name 的大小写
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        value = mapper.readValue(caseInsensitiveInputJson, City.class);
        assertThat(value.getDistance()).isEqualTo(DistanceEnumSimple.KILOMETER);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CityWithValue {
        private DistanceEnumWithValue distance;
    }

    @Test
    @SneakyThrows
    public void testDeserializationWithJsonValue() {
        JsonMapper mapper = JsonMapper.builder().build();
        // language=JSON
        String inputJson = """
                {
                  "distance": "1609.34"
                }""";
        // 枚举项上标记了 @JsonValue，以标记的 @JsonValue 的值为准
        CityWithValue value = mapper.readValue(inputJson, CityWithValue.class);
        assertThat(value.getDistance()).isEqualTo(DistanceEnumWithValue.MILE);
    }

    @AllArgsConstructor
    private enum DistanceWithJsonProperty {
        @JsonProperty("distance-in-km")
        KILOMETER("km", 1000),
        @JsonProperty("distance-in-miles")
        MILE("miles", 1609.34);

        @Setter
        @Getter
        private String unit;

        @Getter
        private final double meters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CityWithJsonPropertyEnum {
        private DistanceWithJsonProperty distance;
    }

    @Test
    @SneakyThrows
    public void testDeserializationWithJsonProperty() {
        JsonMapper mapper = JsonMapper.builder().build();
        // language=JSON
        String inputJson = """
                {
                  "distance": "distance-in-km"
                }
                """;
        // 如果枚举项被 @JsonProperty 标记，以其声明的值作为序列化值
        CityWithJsonPropertyEnum value = mapper.readValue(inputJson, CityWithJsonPropertyEnum.class);
        assertThat(value.getDistance()).isEqualTo(DistanceWithJsonProperty.KILOMETER);
    }

    @AllArgsConstructor
    private enum DistanceEnumWithJsonCreator {
        MILE("miles", 1609.34),

        // --snip--
        ;

        @Setter
        @Getter
        private String unit;

        @Getter(onMethod_ = {@JsonValue})
        private final double meters;

        @JsonCreator
        public static DistanceEnumWithJsonCreator forValues(@JsonProperty("unit") String unit,
                                                            @JsonProperty("meters") double meters) {
            for (DistanceEnumWithJsonCreator distance : DistanceEnumWithJsonCreator.values()) {
                if (distance.unit.equals(unit) && Double.compare(distance.meters, meters) == 0) {
                    return distance;
                }
            }
            return null;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CityWithJsonCreatorEnum {
        private DistanceEnumWithJsonCreator distance;
    }

    @Test
    @SneakyThrows
    public void testDeserializationWithJsonCreator() {
        // language=JSON
        String inputJson = """
                {
                  "distance": {
                    "unit": "miles",
                    "meters": 1609.34
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        CityWithJsonCreatorEnum value = mapper.readValue(inputJson, CityWithJsonCreatorEnum.class);
        assertThat(value.getDistance()).isEqualTo(DistanceEnumWithJsonCreator.MILE);
    }

    @AllArgsConstructor
    @JsonDeserialize(using = CustomEnumDeserializer.class)
    private enum DistanceWithCustomDeserializer {
        KILOMETER("km", 1000),
        MILE("miles", 1609.34);

        @Setter
        @Getter
        private String unit;

        @Getter
        private final double meters;
    }

    private static class CustomEnumDeserializer extends StdDeserializer<DistanceWithCustomDeserializer> {

        @Serial
        private static final long serialVersionUID = 1849163419746192250L;

        public CustomEnumDeserializer() {
            super(DistanceWithCustomDeserializer.class);
        }

        public CustomEnumDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public DistanceWithCustomDeserializer deserialize(JsonParser jsonParser,
                                                          DeserializationContext ctxt) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            String unit = node.get("unit").asText();
            double meters = node.get("meters").asDouble();

            for (DistanceWithCustomDeserializer distance : DistanceWithCustomDeserializer.values()) {
                if (distance.getUnit().equals(unit) && Double.compare(
                        distance.getMeters(), meters) == 0) {
                    return distance;
                }
            }
            return null;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CityWithCustomDeserializerEnum {
        private DistanceWithCustomDeserializer distance;
    }

    @Test
    @SneakyThrows
    public void testCustomDeserialization() {
        // language=JSON
        String inputJson = """
                {
                  "distance": {
                    "unit": "miles",
                    "meters": 1609.34
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        CityWithCustomDeserializerEnum value = mapper.readValue(inputJson, CityWithCustomDeserializerEnum.class);
        assertThat(value.getDistance()).isEqualTo(DistanceWithCustomDeserializer.MILE);
    }

    private enum MyEnum {
        A,
        B
    }

    @Getter
    @Setter
    private static class MyObject {
        private MyEnum myEnum;
    }

    @Test
    @SneakyThrows
    public void testSerializeEnumField() {
        MyObject object = new MyObject();
        object.setMyEnum(MyEnum.A);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(object);
        // language=JSON
        String expectJson = """
                {
                  "myEnum": "A"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
