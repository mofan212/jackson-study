package indi.mofan.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/11 15:13
 */
public class CustomDateSerializer extends StdSerializer<Date> {

    @Serial
    private static final long serialVersionUID = -5221028824855331913L;

    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    public CustomDateSerializer() {
        this(null);
    }

    public CustomDateSerializer(Class<Date> t) {
        super(t);
    }

    @Override
    public void serialize (Date value, JsonGenerator gen, SerializerProvider arg2) throws IOException {
        gen.writeString(FORMAT.format(value));
    }
}