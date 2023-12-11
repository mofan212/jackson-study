package indi.mofan.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2023/12/11 15:30
 */
public class CustomJodaDateTimeSerializer extends StdSerializer<DateTime> {

    @Serial
    private static final long serialVersionUID = 8629534096895903867L;

    private final static DateTimeFormatter FORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public CustomJodaDateTimeSerializer() {
        this(null);
    }

    public CustomJodaDateTimeSerializer(Class<DateTime> t) {
        super(t);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider arg2) throws IOException {
        gen.writeString(FORMATTER.print(value));
    }
}
