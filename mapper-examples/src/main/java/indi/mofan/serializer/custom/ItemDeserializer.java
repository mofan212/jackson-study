package indi.mofan.serializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import indi.mofan.serializer.pojo.Item;
import indi.mofan.serializer.pojo.User;

import java.io.IOException;
import java.io.Serial;

/**
 * @author mofan
 * @date 2023/12/3 22:45
 */
public class ItemDeserializer extends StdDeserializer<Item> {

    @Serial
    private static final long serialVersionUID = 6046140359326104033L;

    public ItemDeserializer() {
        this(null);
    }

    protected ItemDeserializer(Class<?> v) {
        super(v);
    }

    @Override
    public Item deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Item item = new Item();
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        // id
        JsonNode id = node.get("id");
        item.setId(id.asInt());

        // itemName
        JsonNode itemName = node.get("itemName");
        item.setItemName(itemName.asText());

        // owner
        User user = new User();
        JsonNode ownerId = node.get("owner");
        user.setId(ownerId.asInt());
        item.setOwner(user);

        return item;
    }
}
