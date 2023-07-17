package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.jayway.jsonassert.JsonAssert;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * @author mofan
 * @date 2023/5/7 19:23
 */
public class IgnoreTest implements WithAssertions {

    @Getter
    @Setter
    static class People {
        private String name;
        private int age;
        private String desc;
    }

    @Test
    @SneakyThrows
    public void testSerializationInclusionNonNull() {
        People people = new People();
        people.setName("默烦");
        people.setAge(21);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(people);
        JsonAssert.with(json).assertNotDefined("desc")
                .assertThat("$.name", equalTo("默烦"))
                .assertThat("$.age", equalTo(21));
    }

    @Test
    @SneakyThrows
    public void testFailOnUnknownProperties() {
        //language=JSON
        String json = """
                {
                  "name": "默烦",
                  "age": 21,
                  "unknown": "UNKNOWN"
                }
                """;
        ObjectMapper mapper = new ObjectMapper();
        assertThatThrownBy(() -> mapper.readValue(json, People.class))
                .isInstanceOf(UnrecognizedPropertyException.class);

        // configure-first-then-use
        ObjectMapper anotherMapper = new ObjectMapper();
        // 效果与 @JsonIgnoreProperties(ignoreUnknown = true) 一样
        anotherMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        People people = anotherMapper.readValue(json, People.class);
        assertThat(people).extracting("name", "age")
                .containsExactly("默烦", 21);
    }

    @Getter
    @Setter
    @JsonFilter("idCardNumFilter")
    static class Person {
        private String name;
        private Integer age;
        private String idCardNum;
        @JsonIgnore
        private String otherSensitiveInfo;
        @JsonFilter("locationFilter")
        private Company company;
    }

    @Getter
    @Setter
    static class Company {
        private String name;
        private String location;
    }

    @Test
    @SneakyThrows
    public void testJsonFilter() {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        // 序列化时忽略 idCardNum 字段
        filterProvider.addFilter("idCardNumFilter", SimpleBeanPropertyFilter.serializeAllExcept("idCardNum"))
                .addFilter("locationFilter", SimpleBeanPropertyFilter.serializeAllExcept("location"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(filterProvider);

        Person person = new Person();
        person.setName("默烦");
        person.setAge(21);
        person.setIdCardNum("XXX");
        person.setOtherSensitiveInfo("ABC");
        Company company = new Company();
        company.setName("百年不倒股份有限公司");
        company.setLocation("Have a guess");
        person.setCompany(company);

        String json = mapper.writeValueAsString(person);
        JsonAssert.with(json).assertNotDefined("idCardNum")
                .assertNotDefined("otherSensitiveInfo")
                .assertNotDefined("$.company.location")
                .assertThat("$.name", equalTo("默烦"))
                .assertThat("$.age", equalTo(21));


        //language=JSON
        String deserializedJson = """
                {
                  "name": "默烦",
                  "age": 21,
                  "idCardNum": "XXX",
                  "otherSensitiveInfo": "ABC",
                  "company": {
                    "name": "百年不倒股份有限公司",
                    "location": "UNKNOWN"
                  }
                }
                """;
        Person newPerson = mapper.readValue(deserializedJson, Person.class);
        assertThat(newPerson).hasNoNullFieldsOrPropertiesExcept("otherSensitiveInfo")
                .extracting("idCardNum", "company.location")
                .containsExactly("XXX", "UNKNOWN");
    }

    /**
     * @see MixInTest
     */
    @Test
    @SneakyThrows
    public void testSelectiveFilter() {
        SimpleObj obj = new SimpleObj();
        obj.setA("a");
        obj.setB("b");
        obj.setC("c");
        obj.setD("d");

        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider provider = new SimpleFilterProvider();
        provider.addFilter("selectiveFilter", SimpleBeanPropertyFilter.serializeAllExcept("c", "d"));
        mapper.setFilterProvider(provider)
                .addMixIn(SimpleObj.class, SelectiveFilterMixIn.class);
        JsonAssert.with(mapper.writeValueAsString(obj))
                .assertNotDefined("c")
                .assertNotDefined("d")
                .assertEquals("a", "a")
                .assertEquals("b", "b");
    }

    @Getter
    @Setter
    static class SimpleObj {
        private String a;
        private String b;
        private String c;
        private String d;
    }

    @JsonFilter("selectiveFilter")
    static class SelectiveFilterMixIn {
    }
}
