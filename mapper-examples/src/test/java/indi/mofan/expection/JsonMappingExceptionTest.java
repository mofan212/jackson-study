package indi.mofan.expection;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/13 17:17
 * @link <a href="https://www.baeldung.com/jackson-exception">Jackson Exceptions – Problems and Solutions</a>
 */
public class JsonMappingExceptionTest implements WithAssertions {

    private static class Zoo {
        public Animal animal;
    }

    private static abstract class Animal {
        public String name;
    }

    private static class Cat extends Animal {
        public int lives;
    }

    @Getter
    private static class Zoo_1 {
        public Animal_1 animal;
    }

    @Getter
    @JsonDeserialize(as = Cat_1.class)
    private static abstract class Animal_1 {
        public String name;
    }

    private static class Cat_1 extends Animal_1 {
        public int lives;
    }


    @Test
    @SneakyThrows
    public void testCanNotConstructInstanceOf() {
        String inputJson = """
                {
                  "animal": {
                    "name": "lacy"
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                // 不能直接反序列化为抽象类
                .isThrownBy(() -> mapper.readValue(inputJson, Zoo.class))
                .withMessageContaining("Cannot construct instance of");

        // 在抽象类上使用 @JsonDeserialize 注解指定反序列化时的子类
        Zoo_1 value = mapper.readValue(inputJson, Zoo_1.class);
        assertThat(value).extracting(i -> i.getAnimal().getName())
                .isEqualTo("lacy");
    }

    @AllArgsConstructor
    private static class User {
        public int id;
        public String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User_1 {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testNoSuitableConstructor() {
        //language=JSON
        String inputJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                // 没有默认无参构造方法
                .isThrownBy(() -> mapper.readValue(inputJson, User.class))
                // 不会再抛出 No suitable constructor found 的错误信息了
                .withMessageNotContaining("No suitable constructor found")
                .withMessageContaining("Cannot construct instance of");

        User_1 value = mapper.readValue(inputJson, User_1.class);
        assertThat(value).extracting(User_1::getId, User_1::getName)
                .containsExactly(1, "John");
    }
}
