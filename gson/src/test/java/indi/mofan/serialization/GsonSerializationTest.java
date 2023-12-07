package indi.mofan.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import indi.mofan.pojo.ActorGson;
import indi.mofan.pojo.Movie;
import indi.mofan.pojo.MovieWithNullValue;
import indi.mofan.serializer.ActorGsonDeserializer;
import indi.mofan.serializer.ActorGsonSerializer;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * @author mofan
 * @date 2023/12/7 15:51
 * @link <a href="https://www.baeldung.com/jackson-vs-gson">Jackson vs Gson</a>
 */
public class GsonSerializationTest implements WithAssertions {

    @Test
    @SneakyThrows
    public void testSimpleSerialization() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        ActorGson rudyYoungblood = new ActorGson(
                "nm2199632",
                sdf.parse("21-09-1982"),
                List.of("Apocalypto", "Beatdown", "Wind Walkers")
        );
        Movie movie = new Movie(
                "tt0472043",
                "Mel Gibson",
                List.of(rudyYoungblood));

        String result = new Gson().toJson(movie);
        JsonAssertions.assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isObject()
                .containsEntry("imdbId", "tt0472043")
                .containsEntry("director", "Mel Gibson")
                .node("actors")
                .isArray()
                .hasSize(1)
                .singleElement()
                .isObject()
                .containsEntry("imdbId", "nm2199632")
                .containsKey("dateOfBirth")
                .node("filmography")
                .isArray()
                .hasSize(3)
                .contains("Apocalypto", "Beatdown", "Wind Walkers");
    }

    @Test
    @SneakyThrows
    public void testCustomSerialization() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                // 排除不带有 @Expose 注解的字段
                .excludeFieldsWithoutExposeAnnotation()
                // 序列化 null
                .serializeNulls()
                // 不包含 HTML 转义
                .disableHtmlEscaping()
                // ActorGson 使用指定的序列化器进行序列化
                .registerTypeAdapter(ActorGson.class, new ActorGsonSerializer())
                .create();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        ActorGson rudyYoungblood = new ActorGson("nm2199632",
                sdf.parse("21-09-1982"), List.of("Apocalypto", "Beatdown", "Wind Walkers"));

        MovieWithNullValue movieWithNullValue = new MovieWithNullValue(null,
                "Mel Gibson", List.of(rudyYoungblood));

        String result = gson.toJson(movieWithNullValue);
        String expectJson = """
                {
                  "imdbId": null,
                  "actors": [
                    {
                      "IMDB Code": "nm2199632",
                      "Date Of Birth": "21-09-1982",
                      "N° Film: ": 3,
                      "filmography": "Apocalypto-Beatdown-Wind Walkers"
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    // 内部存在 HTML 字符
    static final String JSON_INPUT = "{\"imdbId\":\"tt0472043\",\"actors\":" +
                                     "[{\"imdbId\":\"nm2199632\",\"dateOfBirth\":\"1982-09-21T12:00:00+01:00\"," +
                                     "\"filmography\":[\"Apocalypto\",\"Beatdown\",\"Wind Walkers\"]}]}";

    static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Test
    @SneakyThrows
    public void testDeserialization() {
        Gson gson = new Gson();
        Movie movie = gson.fromJson(JSON_INPUT, Movie.class);
        assertThat(movie.getImdbId()).isEqualTo("tt0472043");
        assertThat(movie.getDirector()).isNull();

        assertThat(movie.getActors()).hasSize(1)
                .singleElement()
                .extracting(
                        ActorGson::getImdbId,
                        i -> FORMAT.format(i.getDateOfBirth()),
                        ActorGson::getFilmography)
                .containsExactly(
                        "nm2199632",
                        "1982-09-21 19:00:00",
                        List.of("Apocalypto", "Beatdown", "Wind Walkers"));
    }

    @Test
    public void testCustomDeserialization() {
        Gson gson = new GsonBuilder()
                // 使用指定的反序列化器反序列化 ActorGson
                .registerTypeAdapter(ActorGson.class,new ActorGsonDeserializer())
                .create();

        Movie outputMovie = gson.fromJson(JSON_INPUT, Movie.class);
        assertThat(outputMovie.getImdbId()).isEqualTo("tt0472043");
        assertThat(outputMovie.getDirector()).isNull();

        assertThat(outputMovie.getActors()).hasSize(1)
                .singleElement()
                .extracting(
                        ActorGson::getImdbId,
                        i -> FORMAT.format(i.getDateOfBirth()),
                        ActorGson::getFilmography)
                .containsExactly(
                        "nm2199632",
                        "1982-09-21 12:00:00",
                        List.of("Apocalypto", "Beatdown", "Wind Walkers"));
    }
}
