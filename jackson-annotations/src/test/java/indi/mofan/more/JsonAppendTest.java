package indi.mofan.more;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2024/1/4 10:56
 */
public class JsonAppendTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Bean {
        private int id;
        private String name;
    }

    @JsonAppend(attrs = {
            @JsonAppend.Attr(value = "version")
    })
    private static class BeanMixIn {
    }

    @Test
    @SneakyThrows
    public void test() {
        Bean bean = new Bean();
        bean.setId(2);
        bean.setName("Bean Name");

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writerFor(Bean.class)
                .withAttribute("version", "1.0")
                .writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 2,
                  "name": "Bean Name"
                }
                """;
        // 没有 version 字段
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson)
                .node("version")
                .isAbsent();

        mapper = JsonMapper.builder()
                .addMixIn(Bean.class, BeanMixIn.class)
                .build();
        result = mapper.writerFor(Bean.class)
                .withAttribute("version", "1.0")
                .writeValueAsString(bean);
        expectJson = """
                {
                  "id": 2,
                  "name": "Bean Name",
                  "version": "1.0"
                }""";
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
