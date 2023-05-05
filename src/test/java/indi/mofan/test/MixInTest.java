package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
                .assertThat("$.str", Matchers.equalTo("mofan"));
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
}
