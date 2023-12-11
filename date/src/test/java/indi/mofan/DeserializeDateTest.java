package indi.mofan;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import indi.mofan.pojo.Event;
import indi.mofan.pojo.EventWithLocalDate;
import indi.mofan.pojo.EventWithSerializer;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author mofan
 * @date 2023/12/11 22:49
 */
public class DeserializeDateTest implements WithAssertions {
    @Test
    @SneakyThrows
    public void testDeserializeEvent() {
        // language=JSON
        String json = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014 02:30:00"
                }""";

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        JsonMapper mapper = JsonMapper.builder()
                .defaultDateFormat(df)
                .build();

        Event event = mapper.readValue(json, Event.class);
        assertThat(event).extracting(Event::getName, i -> df.format(i.getEventDate()))
                .containsExactly("party", "20-12-2014 02:30:00");
    }

    @Test
    @SneakyThrows
    public void testDeserializeJodaZonedDateTimeWithTimeZonePreserved() {
        JsonMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                // 序列化或反序列化时保留时区
                .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        String converted = mapper.writeValueAsString(now);
        ZonedDateTime restored = mapper.readValue(converted, ZonedDateTime.class);
        assertThat(restored.getZone()).isNotEqualTo(now.getZone());
        assertThat(restored.toLocalDateTime().toString()).isNotEqualTo(now.toLocalDateTime().toString());

        mapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
                // 不调整 ZonedDateTime 的时区
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .build();
        restored = mapper.readValue(converted, ZonedDateTime.class);
        assertThat(restored.getZone()).isEqualTo(now.getZone());
        assertThat(restored.toLocalDateTime().toString()).isEqualTo(now.toLocalDateTime().toString());
    }

    @Test
    @SneakyThrows
    public void testCustomDeserializer() {
        // language=JSON
        String json = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014 02:30:00"
                }""";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        JsonMapper mapper = JsonMapper.builder().build();
        EventWithSerializer value = mapper.readValue(json, EventWithSerializer.class);
        assertThat(value).extracting(EventWithSerializer::getName, i -> df.format(i.getEventDate()))
                .containsExactly("party", "20-12-2014 02:30:00");
    }

    @Test
    @SneakyThrows
    public void testDeserializeSingleDateObject() {
        String stringDate = "\"2014-12-20\"";
        JsonMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        LocalDate localDate = mapper.readValue(stringDate, LocalDate.class);
        assertThat(localDate).isEqualTo(LocalDate.of(2014, 12, 20));
    }
    
    @Test
    @SneakyThrows
    public void testDeserializeLocalDateWithoutJavaTimeModule() {
        // language=JSON
        String json = """
                {
                  "name": "party",
                  "eventDate": "20-12-2014"
                }""";
        JsonMapper mapper = JsonMapper.builder().build();
        EventWithLocalDate value = mapper.readValue(json, EventWithLocalDate.class);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        assertThat(value).extracting(EventWithLocalDate::getName, i -> format.format(i.getEventDate()))
                .containsExactly("party", "20-12-2014");
    }
}
