package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>
 * {@code @JsonFormat} 用于指定序列化时 {@link java.util.Date} 类型值的格式
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 14:46
 * @link <a href="https://www.baeldung.com/jackson-annotations#2-jsonformat">JsonFormat</a>
 */
public class JsonFormatTest implements WithAssertions {

    @AllArgsConstructor
    private static class EventWithFormat {
        public String name;

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                // hh 表示 12 小时制，kk 才是 24 小时制
                pattern = "yyyy-MM-dd kk:mm:ss",
                // 设置序列化时使用的时区
                timezone = "GMT+8:00"
        )
        public Date eventDate;
    }

    @Test
    @SneakyThrows
    public void testJsonFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        String toParse = "2023-12-03 14:50:00";
        Date date = df.parse(toParse);
        EventWithFormat event = new EventWithFormat("party", date);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(event);
        //language=JSON
        String expectJson = """
                {
                  "name": "party",
                  "eventDate": "2023-12-03 14:50:00"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
