package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author mofan
 * @date 2023/12/15 17:56
 * @link <a href="https://www.baeldung.com/jackson-booleans-as-integers">Serialize and Deserialize Booleans as Integers With Jackson</a>
 */
public class SerializeBooleansAsIntegersTest implements WithAssertions {
    @Getter
    @Setter
    @NoArgsConstructor
    private static class Game {
        private Long id;
        private String name;
        private Boolean paused;
        private Boolean over;

        public Game(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    @SneakyThrows
    public void testDefaultSerialize() {
        Game game = new Game(1L, "My Game");
        game.setPaused(true);
        game.setOver(false);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(game);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": true,
                  "over": false
                }
                """;
        // 布尔类型的字段被转换成 true/false
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class GameWithJsonFormat {
        private Long id;
        private String name;
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Boolean paused;
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Boolean over;

        public GameWithJsonFormat(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    @SneakyThrows
    public void testFieldLevelConfiguration() {
        GameWithJsonFormat game = new GameWithJsonFormat(1L, "My Game");
        game.setPaused(true);
        game.setOver(false);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(game);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": 1,
                  "over": 0
                }
                """;
        // 布尔类型的字段被转换成了数字 0/1
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testGlobalConfiguration() {
        JsonMapper mapper = JsonMapper.builder()
                // 全局配置，将布尔转成 0/1
                .withConfigOverride(Boolean.class, config -> config.setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER)))
                .build();
        Game game = new Game(1L, "My Game");
        game.setPaused(true);
        game.setOver(false);
        String result = mapper.writeValueAsString(game);
        String expectJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": 1,
                  "over": 0
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testDefaultDeserialize() {
        // language=JSON
        String inputJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": 1,
                  "over": 0
                }""";
        JsonMapper mapper = JsonMapper.builder().build();
        // Jackson 默认支持将 0/1 反序列化成布尔类型的值，而无需其他配置
        Game game = mapper.readValue(inputJson, Game.class);
        assertThat(game).extracting(Game::getPaused, Game::getOver)
                .containsExactly(true, false);
    }

    private static class NumericBooleanSerializer extends JsonSerializer<Boolean> {
        @Override
        public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value ? "1" : "0");
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class GameWithCustomSerializer {
        private Long id;
        private String name;
        @JsonSerialize(using = NumericBooleanSerializer.class)
        @JsonDeserialize(using = NumericBooleanDeserializer.class)
        private Boolean paused;
        @JsonSerialize(using = NumericBooleanSerializer.class)
        @JsonDeserialize(using = NumericBooleanDeserializer.class)
        private Boolean over;

        public GameWithCustomSerializer(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    @SneakyThrows
    public void testSerializeIntoNumericStrings() {
        GameWithCustomSerializer game = new GameWithCustomSerializer(1L, "My Game");
        game.setPaused(true);
        game.setOver(false);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(game);
        // language=JSON
        String expectJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": "1",
                  "over": "0"
                }
                """;
        // 使用 @JsonSerialize 注解只将自定义序列化器作用于特定的字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        mapper = JsonMapper.builder()
                // 全局配置
                .addModule(new SimpleModule().addSerializer(Boolean.class, new NumericBooleanSerializer()))
                .build();
        Game simpleGame = new Game(1L, "My Game");
        simpleGame.setPaused(true);
        simpleGame.setOver(false);
        result = mapper.writeValueAsString(simpleGame);
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class NumericBooleanDeserializer extends JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext context)
                throws IOException {
            if ("1".equals(p.getText())) {
                return Boolean.TRUE;
            }
            if ("0".equals(p.getText())) {
                return Boolean.FALSE;
            }
            return null;
        }
    }

    @Test
    @SneakyThrows
    public void testDeserializeFromNumericStrings() {
        // language=JSON
        String inputJson = """
                {
                  "id": 1,
                  "name": "My Game",
                  "paused": "1",
                  "over": "0"
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        // 使用 @JsonDeserialize 注解只将自定义序列化器作用于特定的字段
        GameWithCustomSerializer value = mapper.readValue(inputJson, GameWithCustomSerializer.class);
        assertThat(value).extracting(GameWithCustomSerializer::getPaused, GameWithCustomSerializer::getOver)
                .containsExactly(true, false);

        // 当然也可以全局配置
        mapper = JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Boolean.class, new NumericBooleanDeserializer()))
                .build();
        Game game = mapper.readValue(inputJson, Game.class);
        assertThat(game).extracting(Game::getPaused, Game::getOver)
                .containsExactly(true, false);
    }
}
