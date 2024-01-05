package indi.mofan.more;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * {@link JsonTypeId} 注解的作用和 {@link com.fasterxml.jackson.annotation.JsonTypeName} 注解的作用类似，
 * 他们都能够在序列化、反序列化时为多态类指定类型 id。{@code @JsonTypeName} 作用在类上，定义的 id 都是静态的；
 * {@code @JsonTypeId} 作用在方法、字段、参数上，以它们的值作为 id，可以动态变化。
 *
 * @author mofan
 * @date 2024/1/5 15:51
 */
public class JsonTypeIdTest implements WithAssertions {

    @Getter
    @Setter
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    @NoArgsConstructor
    @AllArgsConstructor
    private static abstract class Animal {
        private String name;
    }

    private static class Dog extends Animal {
        public Dog(String name) {
            super(name);
        }

        @JsonTypeId
        public String getType() {
            return "dog";
        }
    }

    private static class Cat extends Animal {
        public Cat(String name) {
            super(name);
        }

        @JsonTypeId
        public String getType() {
            return "cat";
        }
    }

    @Test
    @SneakyThrows
    public void test() {
        Dog dog = new Dog("小黑");
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(dog);
        // language=JSON
        String expectJson = """
                {
                  "type": "dog",
                  "name": "小黑"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Cat cat = new Cat("小花");
        result = mapper.writeValueAsString(cat);
        expectJson = """
                {
                  "type": "cat",
                  "name": "小花"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
