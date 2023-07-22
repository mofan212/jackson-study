package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.jackson.Jacksonized;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/7/22 11:04
 */
public class BuilderDesignPatternTest implements WithAssertions {

    @Getter
    @Builder
    @JsonDeserialize(builder = Person.Builder.class)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Person {
        private final String name;
        private final Integer age;

        // 并没有提供 builder() 方法

        @JsonPOJOBuilder
        static class Builder {
            private String name;
            private Integer age;

            Builder withName(String name) {
                this.name = name;
                return this;
            }

            Builder withAge(Integer age) {
                this.age = age;
                return this;
            }

            public Person build() {
                return new Person(name, age);
            }
        }
    }

    private static final String JSON = """
            {
              "name": "mofan",
              "age": 21
            }
            """;

    @Test
    @SneakyThrows
    public void testSimplyUse() {
        Person person = new ObjectMapper().readValue(JSON, Person.class);
        assertThat(person).extracting(Person::getName, Person::getAge)
                .containsExactly("mofan", 21);
    }

    @Getter
    @Jacksonized
    @Builder(builderMethodName = "hiddenBuilder")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Student {
        @JsonProperty("studentName")
        @JsonAlias({"stuName", "stu_name"})
        private final String name;
        private final Integer age;

        public static StudentBuilder builder(String name) {
            return hiddenBuilder().name(name);
        }
    }

    private static final String FIRST_STUDENT_JSON = """
            {
              "studentName": "mofan",
              "age": 21
            }
            """;

    private static final String SECOND_STUDENT_JSON = """
            {
              "stu_name": "默烦",
              "age": 0
            }
            """;

    @Test
    @SneakyThrows
    public void testUseWithLombok() {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readValue(FIRST_STUDENT_JSON, Student.class))
                .extracting(Student::getName, Student::getAge)
                .containsExactly("mofan", 21);
        // 测试别名
        assertThat(mapper.readValue(SECOND_STUDENT_JSON, Student.class))
                .extracting(Student::getName, Student::getAge)
                .containsExactly("默烦", 0);
    }
}
