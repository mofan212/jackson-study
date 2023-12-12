package indi.mofan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/5/7 21:53
 */
public class JsonAutoDetectTest implements WithAssertions {
    static class Student {
        private String name;
        private Integer age;

        public static Student of(String name, Integer age) {
            Student student = new Student();
            student.name = name;
            student.age = age;
            return student;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonAutoDetect() {
        ObjectMapper mapper = new ObjectMapper();
        // 也可以在目标类上使用 @JsonAutoDetect 注解
        mapper.setVisibility(
                mapper.getVisibilityChecker()
                        // 利用反射，不依赖 Getter/Setter
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        );

        Student student = Student.of("默烦", 21);
        String json = mapper.writeValueAsString(student);
        JsonAssert.with(json)
                .assertEquals("name", "默烦")
                .assertEquals("age", 21);

        // 反序列化时必须要求有无参构造或者 Jackson 注解显式指定的用于反序列化的构造器
        assertThat(mapper.readValue(json, Student.class))
                .extracting("name", "age")
                .containsExactly("默烦", 21);
    }
}
