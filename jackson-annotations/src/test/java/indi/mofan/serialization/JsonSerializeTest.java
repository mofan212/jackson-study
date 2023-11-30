package indi.mofan.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/1 11:12
 */
public class JsonSerializeTest implements WithAssertions {

    @AllArgsConstructor
    static class EventWithSerializer {
        public String name;

        @JsonSerialize(using = CustomDateSerializer.class)
        public Date eventDate;
    }

    static class CustomDateSerializer extends StdSerializer<Date> {
        @Serial
        private static final long serialVersionUID = 4625800770970871447L;

        private static final SimpleDateFormat FORMATTER
                = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        public CustomDateSerializer() {
            this(null);
        }

        public CustomDateSerializer(Class<Date> t) {
            super(t);
        }

        @Override
        public void serialize(Date date,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(FORMATTER.format(date));
        }
    }

    @Test
    @SneakyThrows
    public void testJsonSerialize() {
        SimpleDateFormat df
                = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        String toParse = "01-12-2023 02:12:00";
        Date date = df.parse(toParse);
        EventWithSerializer event = new EventWithSerializer("party", date);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(event);
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "2023-12-01 02:12:00"
                }
                """;
        // 使用 @JsonSerialize 注解指定序列化器，将 Date 类型数据转换为 yyyy-MM-dd hh:mm:ss 格式
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
