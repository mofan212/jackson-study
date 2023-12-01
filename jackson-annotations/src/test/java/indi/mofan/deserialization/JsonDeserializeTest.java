package indi.mofan.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/1 16:01
 * @link <a href="https://www.baeldung.com/jackson-annotations#5-jsondeserialize">JsonDeserialize</a>
 */
public class JsonDeserializeTest implements WithAssertions {

    //language=JSON
    public static final String JSON = """
            {
              "name": "party",
              "eventDate": "01-12-2023 02:12:00"
            }
            """;

    @Getter
    static class EventWithSerializer {
        public String name;

        @JsonDeserialize(using = CustomDateDeserializer.class)
        public Date eventDate;
    }

    static class CustomDateDeserializer extends StdDeserializer<Date> {
        @Serial
        private static final long serialVersionUID = 7403477046314138528L;

        private static final SimpleDateFormat FORMATTER
                = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        public CustomDateDeserializer() {
            this(null);
        }

        public CustomDateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String date = parser.getText();
            try {
                return FORMATTER.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @SneakyThrows
    public void testJsonDeserialize() {
        JsonMapper mapper = JsonMapper.builder().build();
        EventWithSerializer event = mapper.readValue(JSON, EventWithSerializer.class);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        assertThat(event).extracting(EventWithSerializer::getEventDate)
                .extracting(df::format)
                .isEqualTo("2023-12-01 02:12:00");
    }
}
