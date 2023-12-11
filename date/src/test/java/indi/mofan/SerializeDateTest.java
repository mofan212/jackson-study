package indi.mofan;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import indi.mofan.pojo.Event;
import indi.mofan.pojo.EventWithFormat;
import indi.mofan.pojo.EventWithSerializer;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author mofan
 * @date 2023/12/11 11:20
 */
public class SerializeDateTest implements WithAssertions {

    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test
    @SneakyThrows
    public void testDate2Timestamp() {
        Date date = FORMAT.parse("01-01-1970 01:00");
        Event event = new Event("party", date);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(event);
        // language=JSON
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": 3600000
                }
                """;
        // 默认情况下，将 Date 类型序列化为时间戳
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testDate2ISO8601() {
        String toParse = "01-01-1970 02:30";
        Date date = FORMAT.parse(toParse);
        Event event = new Event("party", date);

        JsonMapper mapper = JsonMapper.builder()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // StdDateFormat is ISO8601 since jackson 2.9
                .defaultDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .build();
        String result = mapper.writeValueAsString(event);
        //language=JSON
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "1970-01-01T02:30:00.000+00:00"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testConfigureMapperDateFormat() {
        String toParse = "20-12-2014 02:30";
        Date date = FORMAT.parse(toParse);
        Event event = new Event("party", date);

        JsonMapper mapper = JsonMapper.builder()
                .defaultDateFormat(FORMAT)
                .build();

        String result = mapper.writeValueAsString(event);
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014 02:30"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testUseDataFormatAnnotation() {
        String toParse = "20-12-2014 02:30:00";
        Date date = FORMAT.parse(toParse);
        EventWithFormat event = new EventWithFormat("party", date);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(event);
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014 02:30:00"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testCustomDateSerializer() {
        String toParse = "20-12-2014 02:30:00";
        Date date = FORMAT.parse(toParse);
        EventWithSerializer event = new EventWithSerializer("party", date);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(event);
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014 10:30:00"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testSerializeJodaTime() {
        DateTime date = new DateTime(2014, 12, 20, 2, 30,
                DateTimeZone.forID("Europe/London"));

        JsonMapper mapper = JsonMapper.builder()
                .addModule(new JodaModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        String result = mapper.writeValueAsString(date);
        assertThat(result).isEqualTo("\"2014-12-20T02:30:00.000Z\"");
    }

    @Test
    public void testSerializeJodaTimeWithCustomSerializer() {

    }
}
