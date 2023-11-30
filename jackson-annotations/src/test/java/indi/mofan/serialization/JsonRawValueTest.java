package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 10:18
 * @link <a href="https://www.baeldung.com/jackson-annotations#4-jsonrawvalue">JsonRawValue</a>
 */
public class JsonRawValueTest implements WithAssertions {

    @AllArgsConstructor
    static class RawBean {
        public String name;

        @JsonRawValue
        public String json;
    }

    @Test
    @SneakyThrows
    public void testJsonRawValue() {
        RawBean bean = new RawBean("My bean", "{\"attr\":false}");
        JsonMapper mapper = JsonMapper.builder().build();

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "name": "My bean",
                  "json": {
                    "attr": false
                  }
                }
                """;
        /*
         * 如果标注的字段值是一个 JSON 字符串，在序列化时，能够直接将其作为
         * JSON 对象处理。可以通过 @JsonRawValue 的 value 属性来关闭
         * 或启用该功能。
         */
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
