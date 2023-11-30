package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author mofan
 * @date 2023/11/30 16:10
 * @link <a href="https://www.baeldung.com/jackson-annotations#1-jsonanygetter">@JsonAnyGetter</a>
 */
public class JsonAnyGetterTest implements WithAssertions {

    private final JsonMapper mapper = JsonMapper.builder().build();

    private static final Map<String, String> MAP_VALUE = Map.of("attr1", "val1", "attr2", "val2");

    static class ExtendableBean {
        public String name;
        private Map<String, String> properties;

        @JsonAnyGetter
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    @Test
    @SneakyThrows
    public void testOnGetter() {
        ExtendableBean bean = new ExtendableBean();
        bean.name = "My Bean";
        bean.properties = MAP_VALUE;

        String result = mapper.writeValueAsString(bean);
        //language=JSON
        String expectJson = """
                {
                  "name": "My Bean",
                  "attr2": "val2",
                  "attr1": "val1"
                }""";

        // 内部 Map 的值被提取了出来，并与 name 同一层级
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    static class AnotherExtendableBean {
        private Map<String, String> properties;

        @JsonAnyGetter(enabled = false)
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    @Test
    @SneakyThrows
    public void testDisabledJsonAntGetter() {
        AnotherExtendableBean bean = new AnotherExtendableBean();
        bean.properties = MAP_VALUE;

        String result = mapper.writeValueAsString(bean);
        String expectJson = """
                {
                  "properties": {
                    "attr1": "val1",
                    "attr2": "val2"
                  }
                }
                """;

        // 不启用时，作为嵌套对象处理
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
