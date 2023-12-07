package indi.mofan.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import indi.mofan.pojo.ActorGson;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/7 16:35
 */
public class ActorGsonSerializer implements JsonSerializer<ActorGson> {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public JsonElement serialize(ActorGson actor, Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        JsonObject actorJsonObj = new JsonObject();

        actorJsonObj.addProperty("IMDB Code", actor.getImdbId());

        actorJsonObj.addProperty("Date Of Birth",
                actor.getDateOfBirth() != null ?
                        FORMAT.format(actor.getDateOfBirth()) : null);

        actorJsonObj.addProperty("N° Film: ",
                actor.getFilmography()  != null ?
                        actor.getFilmography().size() : null);

        actorJsonObj.addProperty("filmography", actor.getFilmography() != null ?
                convertFilmography(actor.getFilmography()) : null);

        return actorJsonObj;
    }

    private String convertFilmography(List<String> filmography) {
        return String.join("-", filmography);
    }
}
