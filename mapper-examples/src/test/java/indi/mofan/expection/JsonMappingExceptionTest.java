package indi.mofan.expection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        // language=JSON
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

    @Getter
    @JsonRootName(value = "user")
    private static class UserWithRoot {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testRootNameDoesNotMatchExpected() {
        // language=JSON
        String inputJson = """
                {
                  "user": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .build();

        assertThatExceptionOfType(MismatchedInputException.class)
                .isThrownBy(() -> mapper.readValue(inputJson, User_1.class))
                .withMessageContaining("Root name ('user') does not match expected ('User_1')");

        // 使用 @JsonRootName 即可解决
        UserWithRoot user = mapper.readValue(inputJson, UserWithRoot.class);
        assertThat(user).extracting(UserWithRoot::getId, UserWithRoot::getName)
                .containsExactly(1, "John");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserWithPrivateFields {
        int id;
        String name;
    }

    @Test
    @SneakyThrows
    public void testNoSerializerFoundForClass() {
        UserWithPrivateFields user = new UserWithPrivateFields(1, "John");
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> mapper.writeValueAsString(user))
                .withMessageContaining("No serializer found for class");

        // 全局设置可见性
        JsonMapper jsonMapper = JsonMapper.builder()
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .build();
        String result = jsonMapper.writeValueAsString(user);
        String expectJson = """
                {
                  "id": 1,
                  "name": "John"
                }
                """;
        // 还可以在对应的类上使用 @JsonAutoDetect 注解，单独为某个类设置可见性，或者提供 Getter/Setter
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testCanNotDeserializeInstanceOf() {
        // language=JSON
        String inputJson = """
                [
                  {
                    "id": 1,
                    "name": "John"
                  },
                  {
                    "id": 2,
                    "name": "Adam"
                  }
                ]
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(MismatchedInputException.class)
                // 类型不匹配
                .isThrownBy(() -> mapper.readValue(inputJson, User_1.class))
                .withMessageContaining("Cannot deserialize value of type");

        List<User_1> list = mapper.readValue(inputJson, new TypeReference<>() {
        });
        assertThat(list).hasSize(2)
                .extracting(User_1::getId, User_1::getName)
                .containsExactly(tuple(1, "John"), tuple(2, "Adam"));
    }

    @Getter
    private static class Person {
        private String firstName;
        private String lastName;
        private String contact;
    }

    @Getter
    @Setter
    private static class Contact {
        private String email;
    }

    @Getter
    @Setter
    private static class PersonContact {
        private String firstName;
        private String lastName;
        private Contact contact;
    }

    @Test
    @SneakyThrows
    public void testDeserializeStringFromObject() {
        String inputJson = """
                {
                  "firstName":"Azhrioun",
                  "lastName":"Abderrahim",
                  "contact":{
                    "email":"azh@email.com"
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(MismatchedInputException.class)
                // 把 JSON 对象往 String 反序列化
                .isThrownBy(() -> mapper.readValue(inputJson, Person.class))
                .withMessageContaining("Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)");

        PersonContact person = mapper.readValue(inputJson, PersonContact.class);
        assertThat(person).extracting(i -> i.getContact().getEmail())
                .isEqualTo("azh@email.com");
    }

    @Test
    @SneakyThrows
    public void testUnrecognizedPropertyException() {
        String inputJson = """
                {
                  "id": 1,
                  "name": "John",
                  "checked": true
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(UnrecognizedPropertyException.class)
                .isThrownBy(() -> mapper.readValue(inputJson, User_1.class))
                .withMessageContaining("Unrecognized field \"checked\"");

        JsonMapper jsonMapper = JsonMapper.builder()
                // 全局配置，忽略 JSON 中未知字段，也可以在目标类上使用 @JsonIgnoreProperties(ignoreUnknown = true) 注解
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        User_1 value = jsonMapper.readValue(inputJson, User_1.class);
        assertThat(value).extracting(User_1::getId, User_1::getName)
                .containsExactly(1, "John");
    }

    @Test
    @SneakyThrows
    public void testUnexpectedCharacterCode39() {
        String json = "{'id':1,'name':'John'}";
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonParseException.class)
                // 反序列化内容包含单引号
                .isThrownBy(() -> mapper.readValue(json, User_1.class))
                .withMessageContaining("Unexpected character (''' (code 39))");

        JsonMapper jsonMapper = JsonMapper.builder()
                // 全局配置，允许使用单引号
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .build();
        User_1 value = jsonMapper.readValue(json, User_1.class);
        assertThat(value).extracting(User_1::getId, User_1::getName)
                .containsExactly(1, "John");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Book {
        private int id;
        private String title;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BookWithDefaultConstructor {
        private int id;
        private String title;
    }

    @Test
    @SneakyThrows
    public void testNoDefaultConstructor() {
        // language=JSON
        String jsonString = """
                {
                  "id": "10",
                  "title": "Harry Potter"
                }""";
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> mapper.readValue(jsonString, Book.class))
                .withMessageContaining("Cannot construct instance of");

        BookWithDefaultConstructor book = mapper.readValue(jsonString, BookWithDefaultConstructor.class);
        assertThat(book).extracting(BookWithDefaultConstructor::getId, BookWithDefaultConstructor::getTitle)
                .containsExactly(10, "Harry Potter");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Animals {
        private final int id;
        private String name;
    }

    @Getter
    @Setter
    private static class AnimalsWithJsonPropertyAnnotationConstructor {
        private final int id;
        private String name;

        AnimalsWithJsonPropertyAnnotationConstructor(@JsonProperty("id") int id,
                                                     @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    @SneakyThrows
    public void testMissingJsonPropertyAnnotation() {
        // language=JSON
        String jsonString = """
                {
                  "id": 10,
                  "name": "Dog"
                }""";
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(InvalidDefinitionException.class)
                .isThrownBy(() -> mapper.readValue(jsonString, Animals.class))
                .withMessageContaining("Cannot construct instance of");

        // 构造方法参数上使用 @JsonProperty
        AnimalsWithJsonPropertyAnnotationConstructor animals = mapper.readValue(jsonString, AnimalsWithJsonPropertyAnnotationConstructor.class);
        assertThat(animals).extracting(AnimalsWithJsonPropertyAnnotationConstructor::getId, AnimalsWithJsonPropertyAnnotationConstructor::getName)
                .containsExactly(10, "Dog");
    }

    @Test
    @SneakyThrows
    public void testIncompatibleJavaObject() {
        // language=JSON
        String jsonString = """
                [
                  {
                    "id": "10",
                    "title": "Harry Potter"
                  }
                ]""";
        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(MismatchedInputException.class)
                .isThrownBy(() -> mapper.readValue(jsonString, BookWithDefaultConstructor.class))
                .withMessageContaining("Cannot deserialize value of type");

        List<BookWithDefaultConstructor> books = mapper.readValue(jsonString, new TypeReference<>() {
        });
        assertThat(books).hasSize(1)
                .singleElement()
                .extracting(BookWithDefaultConstructor::getId, BookWithDefaultConstructor::getTitle)
                .containsExactly(10, "Harry Potter");
    }
}
