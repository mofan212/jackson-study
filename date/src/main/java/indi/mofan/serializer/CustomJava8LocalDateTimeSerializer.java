package indi.mofan.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author mofan
 * @date 2023/12/11 22:42
 */
public class CustomJava8LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    @Serial
    private static final long serialVersionUID = -5233982302714381302L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CustomJava8LocalDateTimeSerializer() {
        this(null);
    }

    public CustomJava8LocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime localDateTime,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(FORMATTER.format(localDateTime));
    }
}
