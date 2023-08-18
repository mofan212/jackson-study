package indi.mofan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.jayway.jsonassert.JsonAssert;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * @author mofan
 * @date 2023/5/5 20:22
 */
public class MixInTest implements WithAssertions {

    @Test
    @SneakyThrows
    public void testIgnoreType() {
        Target target = new Target();
        target.setDate(new Date());
        target.setStr("mofan");

        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Date.class, IgnoreType.class);
        String json = mapper.writeValueAsString(target);
        JsonAssert.with(json).assertNotDefined("date")
                .assertThat("$.str", equalTo("mofan"));
        assertThat(mapper.readValue(json, Target.class))
                .isNotNull()
                .hasAllNullFieldsOrPropertiesExcept("str")
                .extracting(Target::getStr)
                .isEqualTo("mofan");
    }

    @Test
    public void testConvertAndIgnoreType() {
        Map<String, Object> map = new HashMap<>();
        LocalDate localDate = LocalDate.of(2023, 5, 5);
        Date date = Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.of("+8")));
        map.put("date", date);
        map.put("customType", new CustomType());
        map.put("str", "abc");
        ObjectMapper mapper = new ObjectMapper();
        // 忽略反序列化时在 JSON 中存在、在 Java 对象中不存在的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略 CustomType 类型的序列化或反序列化
        mapper.addMixIn(CustomType.class, IgnoreType.class);
        Target target = mapper.convertValue(map, Target.class);
        assertThat(target).extracting(Target::getDate, Target::getStr)
                .containsExactly(date, "abc");
    }

    @JsonIgnoreType
    static class IgnoreType {
    }

    static class CustomType {
    }

    @Getter
    @Setter
    static class Target {
        private Date date;
        private String str;
    }

    static class Person {

        private final String firstName;
        private final String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    @Test
    @SneakyThrows
    public void testSerializeThirdPartyObject() {
        Person person = new Person("默", "烦");
        ObjectMapper mapper = new ObjectMapper();
        assertThatThrownBy(() -> mapper.writeValueAsString(person))
                .isInstanceOf(InvalidDefinitionException.class);

        // configure-first-then-use，new 一个新的 ObjectMapper 对象
        ObjectMapper anotherMapper = new ObjectMapper();
        anotherMapper.addMixIn(Person.class, PersonMixIn.class);
        String json = anotherMapper.writeValueAsString(person);
        JsonAssert.with(json).assertThat("$.firstName", equalTo("默"))
                .assertThat("$.lastName", equalTo("烦"));
        Person newPerson = anotherMapper.readValue(json, Person.class);
        assertThat(newPerson).extracting("firstName", "lastName")
                .containsExactly("默", "烦");
    }

    abstract static class PersonMixIn {
        @JsonProperty
        private final String firstName;
        @JsonProperty
        private final String lastName;

        @JsonCreator
        public PersonMixIn(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    @Getter
    @Setter
    static class Student {
        private String name;
        private String idCardNum;
    }

    abstract static class StudentMixIn {
        @JsonProperty("NAME")
        private String name;
        @JsonIgnore
        private String idCardNum;
    }

    @Test
    @SneakyThrows
    public void testIgnoreAndChangeThirdPartyObjectProperties() {
        Student student = new Student();
        student.setName("默烦");
        student.setIdCardNum("xxx");

        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Student.class, StudentMixIn.class);
        String json = mapper.writeValueAsString(student);
        JsonAssert.with(json).assertNotDefined("idCardNum")
                .assertThat("$.NAME", equalTo("默烦"));
        Student newStu = mapper.readValue(json, Student.class);
        assertThat(newStu).extracting("name").isEqualTo("默烦");
    }

    private static final String SEPARATOR = "->";

    @Getter
    @EqualsAndHashCode(of = {"realName", "nickName"})
    @JsonSerialize(using = People.PeopleSerializer.class)
    @JsonDeserialize(using = People.PeopleDeserializer.class)
    static class People {
        private final String realName;
        private final String nickName;

        @JsonCreator
        public People(@JsonProperty String realName, @JsonProperty String nickName) {
            this.realName = realName;
            this.nickName = nickName;
        }

        static class PeopleSerializer extends JsonSerializer<People> {
            @Override
            public void serialize(People people, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                // 序列化时，先真实姓名再昵称
                jsonGenerator.writeString(people.getRealName() + SEPARATOR + people.getNickName());
            }
        }

        static class PeopleDeserializer extends JsonDeserializer<People> {
            @Override
            public People deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                String[] split = jsonParser.getValueAsString().split(SEPARATOR);
                return new People(split[0], split[1]);
            }
        }
    }

    @JsonSerialize(using = PeopleMixIn.AnotherPeopleSerializer.class)
    @JsonDeserialize(using = PeopleMixIn.AnotherPeopleDeserializer.class)
    static abstract class PeopleMixIn {
        static class AnotherPeopleSerializer extends JsonSerializer<People> {
            @Override
            public void serialize(People people, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                // 序列化时，先昵称再真实姓名
                jsonGenerator.writeString(people.getNickName() + SEPARATOR + people.getRealName());
            }
        }

        static class AnotherPeopleDeserializer extends JsonDeserializer<People> {
            @Override
            public People deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                String[] split = jsonParser.getValueAsString().split(SEPARATOR);
                return new People(split[1], split[0]);
            }
        }
    }

    @Test
    @SneakyThrows
    public void testChangeCustomSerializer() {
        People people = new People("默烦", "mofan212");

        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(people);
        assertThat(str).isEqualTo("\"默烦->mofan212\"");
        People newPeople = mapper.readValue(str, People.class);
        assertThat(newPeople).isEqualTo(people);

        mapper = new ObjectMapper();
        mapper.addMixIn(People.class, PeopleMixIn.class);
        str = mapper.writeValueAsString(people);
        assertThat(str).isEqualTo("\"mofan212->默烦\"");
        newPeople = mapper.readValue(str, People.class);
        assertThat(newPeople).isEqualTo(people);
    }
}
