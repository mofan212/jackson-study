package indi.mofan.serializer.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import indi.mofan.serializer.pojo.Item;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2023/12/3 22:27
 */
public class ItemSerializer extends StdSerializer<Item> {
    @Serial
    private static final long serialVersionUID = -5830563363657905311L;

    public ItemSerializer() {
        this(null);
    }

    public ItemSerializer(Class<Item> t) {
        super(t);
    }

    @Override
    public void serialize(Item value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 序列化 Item 时，只要 id、itemName、owner
        gen.writeStartObject();
        gen.writeNumberField("id", value.id);
        gen.writeStringField("itemName", value.itemName);
        gen.writeNumberField("owner", value.owner.id);
        gen.writeEndObject();
    }
}
