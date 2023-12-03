package indi.mofan.polymorphic;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * <p>一共涉及到三个注解：</p>
 * <ul>
 *     <li>{@code @JsonTypeInfo}: 指示在序列化时包含的类型信息细节</li>
 *     <li>{@code @JsonSubTypes}: 指示标记类型的子类</li>
 *     <li>{@code @JsonTypeName}: 为标记的类型定义一个逻辑类型名称</li>
 * </ul>
 *
 * @author mofan
 * @date 2023/12/3 14:08
 * @link <a href="https://www.baeldung.com/jackson-annotations#jackson-polymorphic-type-handling-annotations">Jackson Polymorphic Type Handling Annotations</a>
 */
public class PolymorphicTypeHandlingAnnotationsTest implements WithAssertions {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Zoo {
        public Animal animal;

        @Getter
        @JsonTypeInfo(
                // 序列化时使用定义的逻辑类型名称区分子类，使用的 JSON 字段是 type
                use = JsonTypeInfo.Id.NAME,
                property = "type")
        @JsonSubTypes({
                // Animal 序列化时使用的子类有两种，分别是 Dog 和 Cat
                @JsonSubTypes.Type(value = Dog.class, name = "dog"),
                @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Animal {
            public String name;
        }

        @JsonTypeName("dog")
        public static class Dog extends Animal {
            public double barkVolume;

            public Dog(String name) {
                super(name);
            }
        }

        @JsonTypeName("cat")
        public static class Cat extends Animal {
            boolean likesCream;
            public int lives;
        }
    }

    @Test
    @SneakyThrows
    public void testJacksonPolymorphicTypeHandlingAnnotations() {
        Zoo.Dog dog = new Zoo.Dog("lacy");
        Zoo zoo = new Zoo(dog);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(zoo);
        // language=JSON
        String expectJson = """
                {
                  "animal": {
                    "type": "dog",
                    "name": "lacy",
                    "barkVolume": 0.0
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        String inputJson = """
                {
                  "animal": {
                    "name": "lacy",
                    "type": "cat"
                  }
                }
                """;
        Zoo value = mapper.readValue(inputJson, Zoo.class);
        assertThat(value).extracting(Zoo::getAnimal)
                .isInstanceOf(Zoo.Cat.class)
                .extracting(Zoo.Animal::getName)
                .isEqualTo("lacy");
    }
}
