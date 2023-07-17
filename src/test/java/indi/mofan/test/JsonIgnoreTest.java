package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jayway.jsonassert.JsonAssert;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author mofan
 * @date 2023/5/5 20:29
 */
public class JsonIgnoreTest implements WithAssertions {

    @Test
    @SneakyThrows
    public void testJsonIgnore() {
        JsonIgnoreObject object = new JsonIgnoreObject();
        object.setIgnore("ignore");
        object.setStr("str");
        object.setInteger(212);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(object);
        JsonAssert.with(json).assertNotDefined("$.ignore");
        assertThat(mapper.readValue(json, JsonIgnoreObject.class))
                .isNotNull()
                .extracting(JsonIgnoreObject::getStr, JsonIgnoreObject::getInteger, JsonIgnoreObject::getIgnore)
                .containsExactly("str", 212, null);
    }

    @Getter
    @Setter
    static class JsonIgnoreObject {
        private String str;
        private Integer integer;
        @JsonIgnore
        private String ignore;
    }

    @Test
    @SneakyThrows
    public void testJsonIgnoreProperties() {
        JsonIgnorePropertiesObj obj = new JsonIgnorePropertiesObj();
        obj.setIgnore("ignore");
        obj.setInteger(212);
        obj.setStr("str");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(obj);
        JsonAssert.with(json).assertNotDefined("ignore").assertNotDefined("integer");
        assertThat(mapper.readValue(json, JsonIgnorePropertiesObj.class))
                .isNotNull()
                .hasAllNullFieldsOrPropertiesExcept("str");
    }

    @Getter
    @Setter
    @JsonIgnoreProperties({"ignore", "integer"})
    static class JsonIgnorePropertiesObj {
        private String str;
        private String ignore;
        private Integer integer;
    }

    @Test
    @SneakyThrows
    public void testJsonIgnoreType() {
        JsonIgnoreTypeObj obj = new JsonIgnoreTypeObj();
        obj.setInteger(212);
        obj.setStr("str");
        JsonIgnoreTypeObj.Inner inner = new JsonIgnoreTypeObj.Inner();
        inner.setString("string");
        obj.setInner(inner);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(obj);
        JsonAssert.with(json).assertNotDefined("inner")
                .assertThat("$.str", Matchers.equalTo("str"))
                .assertThat("$.integer", Matchers.equalTo(212));

        assertThat(mapper.readValue(json, JsonIgnoreTypeObj.class))
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("inner");
    }

    @Getter
    @Setter
    static class JsonIgnoreTypeObj {
        private String str;
        private Integer integer;
        private Inner inner;

        @Getter
        @Setter
        @JsonIgnoreType
        static class Inner {
            private String string;
        }
    }

    interface ParentInterface {
    }

    @Getter
    @Setter
    static class Subclass implements ParentInterface {
        private String str;
    }

    @Getter
    @Setter
    static class Composite {
        private String string;
        private Integer integer;
        private Subclass subclass;
    }

    @JsonIgnoreType
    static class ParentInterfaceMixIn {
    }

    @Test
    @SneakyThrows
    public void testJsonIgnoreTypeByParentClass() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ParentInterface.class, ParentInterfaceMixIn.class);

        Composite composite = new Composite();
        composite.setString("string");
        composite.setInteger(10);
        Subclass subclass = new Subclass();
        subclass.setStr("str");
        composite.setSubclass(subclass);

        String json = mapper.writeValueAsString(composite);
        JsonAssert.with(json).assertNotDefined("subclass");
    }

    @Getter
    @Setter
    static class User {
        private String username;
        private String pwd;
        private Role role;
    }

    @Getter
    @Setter
    static class Role {
        private String name;
        @JsonIgnoreProperties("role")
        private List<User> users;
    }

    @Test
    @SneakyThrows
    public void testCircularReference() {
        User user = new User();
        user.setUsername("mofan");
        user.setPwd("123456");

        Role role = new Role();
        role.setName("admin");
        role.setUsers(List.of(user));

        user.setRole(role);

        JsonMapper mapper = JsonMapper.builder().build();
        String json = mapper.writeValueAsString(user);
        // language=JSON
        String expectJson = """
                {
                  "username": "mofan",
                  "pwd": "123456",
                  "role": {
                    "name": "admin",
                    "users": [
                      {
                        "username": "mofan",
                        "pwd": "123456"
                      }
                    ]
                  }
                }
                """;
        JsonAssertions.assertThatJson(json).isEqualTo(expectJson);
    }
}
