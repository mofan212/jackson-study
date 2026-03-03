package indi.mofan;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2026/3/3 15:04
 */
public class BeanSerializerModifierTest {

    static final JsonMapper JSON_MAPPER;

    static {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new ModifierObjectSerializerModifier());
        JSON_MAPPER = JsonMapper.builder()
                .addModule(module)
                .build();
    }

    static class ModifierObjectSerializer extends StdSerializer<ModifierObject> {

        @Serial
        private static final long serialVersionUID = 6387491913980871932L;

        private final JsonSerializer<Object> defaultSerializer;

        public ModifierObjectSerializer(JsonSerializer<Object> defaultSerializer) {
            super(ModifierObject.class);
            this.defaultSerializer = defaultSerializer;
        }

        @Override
        public void serialize(ModifierObject modifierObject,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            // 修改数据后，再序列化
            modifierObject.setStr(modifierObject.getStr() + ".");
            // 委托给默认序列化器，避免栈溢出
            defaultSerializer.serialize(modifierObject, jsonGenerator, serializerProvider);
        }
    }

    static class ModifierObjectSerializerModifier extends BeanSerializerModifier {
        @Serial
        private static final long serialVersionUID = 2215529846889222004L;

        @Override
        @SuppressWarnings("unchecked")
        public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                  BeanDescription beanDesc,
                                                  JsonSerializer<?> serializer) {
            // 当 Jackson 准备为 ModifierObject 创建序列化器时进行拦截
            if (beanDesc.getBeanClass() == ModifierObject.class) {
                // 此时的 serializer 就是 Jackson 原本生成的默认序列化器
                return new ModifierObjectSerializer((JsonSerializer<Object>) serializer);
            }
            return serializer;
        }
    }

    @Getter
    @Setter
    static class ModifierObject {
        private String str;
        private Integer num;
    }

    @Test
    @SneakyThrows
    public void testBeanSerializerModifier() {
        ModifierObject modifierObject = new ModifierObject();
        modifierObject.setStr("hello");
        modifierObject.setNum(1);
        String result = JSON_MAPPER.writeValueAsString(modifierObject);
        String expected = """
                {
                    "str": "hello.",
                    "num": 1
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expected);
    }

}
