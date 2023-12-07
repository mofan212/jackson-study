package indi.mofan.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import indi.mofan.pojo.ActorGson;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * @author mofan
 * @date 2023/12/7 17:57
 */
public class ActorGsonDeserializer implements JsonDeserializer<ActorGson> {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Override
    @SneakyThrows
    public ActorGson deserialize(JsonElement json,
                                 Type type,
                                 JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonImdbId = jsonObject.get("imdbId");
        JsonElement jsonDateOfBirth = jsonObject.get("dateOfBirth");
        JsonArray jsonFilmography = jsonObject.getAsJsonArray("filmography");

        ArrayList<String> filmList = new ArrayList<>();
        if (jsonFilmography != null) {
            for (int i = 0; i < jsonFilmography.size(); i++) {
                filmList.add(jsonFilmography.get(i).getAsString());
            }
        }

        return new ActorGson(
                jsonImdbId.getAsString(),
                FORMAT.parse(jsonDateOfBirth.getAsString()),
                filmList
        );
    }
}
