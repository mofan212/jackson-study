package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

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
}
