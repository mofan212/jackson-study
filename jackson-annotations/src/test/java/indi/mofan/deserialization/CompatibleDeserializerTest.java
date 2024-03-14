package indi.mofan.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2024/3/14 16:21
 */
public class CompatibleDeserializerTest implements WithAssertions {
    //language=JSON
    private static final String OLD_DATA_FALSE = """
            {
              "value": false
            }
            """;

    //language=JSON
    private static final String OLD_DATA_TRUE = """
            {
              "value": true
            }
            """;

    //language=JSON
    public static final String NEW_DATA_ENUM_ONE = """
            {
              "value": "ONE"
            }
            """;

    //language=JSON
    public static final String NEW_DATA_ENUM_TWO = """
            {
              "value": "TWO"
            }
            """;

    private enum MyEnum {
        ONE, TWO
    }

    private static class CompatibleSerializer extends StdDeserializer<MyEnum> {

        @Serial
        private static final long serialVersionUID = 1640153564267883299L;

        public CompatibleSerializer() {
            this(null);
        }

        public CompatibleSerializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public MyEnum deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonToken token = parser.getCurrentToken();
            // 如果是布尔
            if (token.isBoolean()) {
                boolean booleanValue = parser.getBooleanValue();
                return booleanValue ? MyEnum.ONE : MyEnum.TWO;
            }
            // 如果是文本
            if (JsonToken.VALUE_STRING.equals(token)) {
                MyEnum enumValue = EnumUtils.getEnum(MyEnum.class, parser.getText());
                return ObjectUtils.defaultIfNull(enumValue, MyEnum.ONE);
            }
            // 默认 NONE
            return MyEnum.ONE;
        }
    }

    @Getter
    @Setter
    private static class MyObject {
        @JsonDeserialize(using = CompatibleSerializer.class)
        private MyEnum value;
    }

    @Test
    @SneakyThrows
    public void test() {
        JsonMapper mapper = JsonMapper.builder().build();
        MyObject object = mapper.readValue(OLD_DATA_FALSE, MyObject.class);
        assertThat(object.getValue()).isEqualTo(MyEnum.TWO);

        object = mapper.readValue(OLD_DATA_TRUE, MyObject.class);
        assertThat(object.getValue()).isEqualTo(MyEnum.ONE);

        object = mapper.readValue(NEW_DATA_ENUM_ONE, MyObject.class);
        assertThat(object.getValue()).isEqualTo(MyEnum.ONE);

        object = mapper.readValue(NEW_DATA_ENUM_TWO, MyObject.class);
        assertThat(object.getValue()).isEqualTo(MyEnum.TWO);
    }
}
