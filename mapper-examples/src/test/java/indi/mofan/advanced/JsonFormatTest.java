package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author mofan
 * @date 2024/2/3 16:54
 * @link <a href="https://www.baeldung.com/jackson-jsonformat">Guide to @JsonFormat in Jackson</a>
 */
public class JsonFormatTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Getter
    @Setter
    @NoArgsConstructor
    private static class User {
        private String firstName;
        private String lastName;
        /**
         * 2024-02-03
         */
        private Date createdDate = new Date(1706803200000L);

        public Date getCreatedDate() {
            return this.createdDate;
        }

        static User createUser() {
            User user = new User();
            user.setFirstName("mo");
            user.setLastName("fan");
            return user;
        }
    }

    @Test
    @SneakyThrows
    public void testUsingDefaultFormat() {
        User user = User.createUser();
        String result = MAPPER.writeValueAsString(user);
        // language=JSON
        String expectJson = """
                {
                  "firstName": "mo",
                  "lastName": "fan",
                  "createdDate": 1706803200000
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class UserMixIn1 {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone = "Asia/Shanghai")
        private Date createdDate;
    }

    private static abstract class UserMixIn2 {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public abstract Date getCurrentDate();
    }

    @Test
    @SneakyThrows
    public void testUsingAnnotationOnGetter() {
        User user = User.createUser();
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(User.class, UserMixIn1.class)
                .build();

        String result = mapper.writeValueAsString(user);
        // language=JSON
        String expectJson = """
                {
                  "firstName": "mo",
                  "lastName": "fan",
                  "createdDate": "2024-02-02@00:00:00.000+0800"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
