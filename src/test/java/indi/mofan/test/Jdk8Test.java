package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * @author mofan
 * @date 2023/7/22 16:38
 */
public class Jdk8Test implements WithAssertions {
    @Getter
    @Setter
    static class TimeObject {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate localDate;
    }

    @Test
    @SneakyThrows
    public void testSupportDateTime() {
        TimeObject object = new TimeObject();
        object.setLocalDate(LocalDate.of(2023, 1, 1));

        assertThatThrownBy(() -> new ObjectMapper().writeValueAsString(object))
                .isExactlyInstanceOf(InvalidDefinitionException.class);

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        String expectJson = """
                {
                  "localDate": "2023-01-01"
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(object))
                .isEqualTo(expectJson);
    }
}
