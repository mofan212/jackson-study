package indi.mofan;

import com.fasterxml.jackson.databind.json.JsonMapper;
import indi.mofan.pojo.Car;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author mofan
 * @date 2023/12/3 22:56
 * @link <a href="https://www.baeldung.com/jackson-object-mapper-tutorial#3-handling-date-formats">Handling Date Formats</a>
 */
public class DateFormatTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Request {
        private Car car;
        private Date datePurchased;
    }

    @Test
    @SneakyThrows
    public void testHandlingDateFormats() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        JsonMapper mapper = JsonMapper.builder()
                .defaultDateFormat(format)
                .build();

        Car car = new Car("yellow", "renault");
        String toParse = "2023-12-03 11:00";
        Request request = new Request(car, format.parse(toParse));
        String result = mapper.writeValueAsString(request);
        String expectJson = """
                {
                  "car": {
                    "color": "yellow",
                    "type": "renault"
                  },
                  "datePurchased": "2023-12-03 11:00"
                }
                """;
        // 按给定的格式序列化了 Date 类型的数据
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

}
