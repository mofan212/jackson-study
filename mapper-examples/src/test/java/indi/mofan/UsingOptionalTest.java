package indi.mofan;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * @author mofan
 * @date 2023/12/6 21:17
 * @link <a href="https://www.baeldung.com/jackson-optional">Using Optional with Jackson</a>
 */
public class UsingOptionalTest implements WithAssertions {

    @Getter
    @Setter
    private static class Book {
        String title;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        Optional<String> subTitle;
    }

    @Test
    @SneakyThrows
    public void testSerialization() {
        Book book = new Book();
        book.setTitle("Oliver Twist");
        book.setSubTitle(Optional.of("The Parish Boy's Progress"));

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(book);
        String expectJson = """
                {
                  "title": "Oliver Twist",
                  "subTitle": {
                    "empty": false,
                    "present": true
                  }
                }
                """;
        // subTitle 的值很奇怪！
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    public void testDeserialization() {
        //language=JSON
        String targetJson = """
                {
                  "title": "Oliver Twist",
                  "subTitle": "foo"
                }""";
        JsonMapper mapper = JsonMapper.builder().build();

        // 很显然，反序列化失败
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.readValue(targetJson, Book.class));
    }

    @Test
    @SneakyThrows
    public void testSolution() {
        Book book = new Book();
        book.setTitle("Oliver Twist");
        book.setSubTitle(Optional.of("The Parish Boy's Progress"));

        JsonMapper mapper = JsonMapper.builder()
                // 添加 Module
                .addModule(new Jdk8Module())
                .build();
        String result = mapper.writeValueAsString(book);
        String expectJson = """
                {
                  "title": "Oliver Twist",
                  "subTitle": "The Parish Boy's Progress"
                }
                """;
        // 序列化时，将 Optional 类型的字段序列化为其对应的值
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Book value = mapper.readValue(result, Book.class);
        // 也能够反序列化成 Optional 类型的值
        assertThat(value.getSubTitle()).isNotEmpty()
                .hasValue("The Parish Boy's Progress");

        book.setSubTitle(Optional.empty());
        result = mapper.writeValueAsString(book);
        JsonAssertions.assertThatJson(result)
                // subTitle = null
                .node("subTitle")
                .isNull();

        value = mapper.readValue(result, Book.class);
        // 如果没有值，反序列化成 Optional.empty()
        assertThat(value.getSubTitle()).isEmpty();
    }

}
