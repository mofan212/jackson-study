package indi.mofan.deserialization;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mofan
 * @date 2023/12/1 15:19
 * @link <a href="https://www.baeldung.com/jackson-annotations#3-jsonanysetter">JsonAnySetter</a>
 */
public class JsonAnySetterTest implements WithAssertions {
    // language=JSON
    public static final String JSON = """
            {
                "name":"My bean",
                "attr2":"val2",
                "attr1":"val1"
            }
            """;

    @Getter
    static class ExtendableBean {
        public String name;
        private final Map<String, String> properties = new HashMap<>();

        @JsonAnySetter
        public void add(String key, String value) {
            properties.put(key, value);
        }
    }

    @Test
    @SneakyThrows
    public void testJsonAnySetter() {
        JsonMapper mapper = JsonMapper.builder().build();
        ExtendableBean bean = mapper.readValue(JSON, ExtendableBean.class);

        assertThat(bean).isNotNull()
                .extracting(ExtendableBean::getName, ExtendableBean::getProperties)
                .contains("My bean", Index.atIndex(0))
                // 和 @JsonAnyGetter 类似，用于将平铺的字段添加到 Map 中
                .contains(Map.of("attr1", "val1", "attr2", "val2"), Index.atIndex(1));
    }
}
