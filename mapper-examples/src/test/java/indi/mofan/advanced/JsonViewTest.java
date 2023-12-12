package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/12 14:37
 * @link <a href="https://www.baeldung.com/jackson-json-view-annotation">Jackson JSON Views</a>
 */
public class JsonViewTest implements WithAssertions {
    private static class Views {
        private static class Public {
        }

        public static class Internal extends Public {
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        public int id;

        @JsonView(Views.Public.class)
        public String name;
    }

    @Test
    @SneakyThrows
    public void testSerializeByView() {
        User user = new User(1, "John");

        JsonMapper mapper = JsonMapper.builder()
                // 禁用默认视图，未被 @JsonView 注解标记的字段用不参与序列化
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();

        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(user);
        String expectJson = """
                {
                  "name": "John"
                }
                """;
        // 只序列化 Views.Public.class 标记的字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        mapper = JsonMapper.builder().build();
        result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(user);
        // language=JSON
        expectJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        // 未禁用 DEFAULT_VIEW_INCLUSION 时，未被 @JsonView 标记的字段默认参与序列化
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Item {

        @JsonView(Views.Public.class)
        public int id;

        @JsonView(Views.Public.class)
        public String itemName;

        @JsonView(Views.Internal.class)
        public String ownerName;
    }

    @Test
    @SneakyThrows
    public void testUseMultipleJsonViews() {
        Item item = new Item(2, "book", "John");

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(item);
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        result = mapper.writerWithView(Views.Internal.class)
                .writeValueAsString(item);
        // language=JSON
        expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "ownerName": "John"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testDeserializeUsingJsonViews() {
        // language=JSON
        String inputJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        User user = mapper.readerWithView(Views.Public.class)
                .readValue(inputJson, User.class);
        assertThat(user).extracting(User::getId, User::getName)
                .containsExactly(1, "John");
    }

    private static class UpperCasingWriter extends BeanPropertyWriter {
        @Serial
        private static final long serialVersionUID = -8579291341211375541L;

        BeanPropertyWriter _writer;

        public UpperCasingWriter(BeanPropertyWriter w) {
            super(w);
            _writer = w;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen,
                                     SerializerProvider prov) throws Exception {
            String value = ((User) bean).name;
            value = (value == null) ? "" : value.toUpperCase();
            gen.writeStringField("name", value);
        }
    }

    private static class MyBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config, BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
            for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter writer = beanProperties.get(i);
                if ("name".equals(writer.getName())) {
                    beanProperties.set(i, new UpperCasingWriter(writer));
                }
            }
            return beanProperties;
        }
    }

    @Test
    @SneakyThrows
    public void testCustomizeJsonViews() {
        User user = new User(1, "John");
        SerializerFactory factory = BeanSerializerFactory.instance
                .withSerializerModifier(new MyBeanSerializerModifier());

        JsonMapper mapper = JsonMapper.builder()
                .serializerFactory(factory)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();

        String result = mapper.writerWithView(Views.Public.class)
                .writeValueAsString(user);
        //language=JSON
        String expectJson = """
                {
                  "name": "JOHN"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
