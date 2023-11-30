package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/11/30 16:40
 * @link <a href="https://www.baeldung.com/jackson-annotations#3-jsonpropertyorder">JsonPropertyOrder</a>
 */
public class JsonPropertyOrderTest implements WithAssertions {

    @AllArgsConstructor
    @JsonPropertyOrder({"name", "id"})
    static class MyBean {
        public int id;
        public String name;
    }

    @Test
    @SneakyThrows
    public void testJsonPropertyOrder() {
        MyBean bean = new MyBean(1, "My bean");
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        String expect = "{\"name\":\"My bean\",\"id\":1}";

        // 指定反序列化后字段的顺序，直接用字符串比较
        assertThat(result).isEqualTo(expect);
    }

    @AllArgsConstructor
    @JsonPropertyOrder(alphabetic = true)
    static class MyAnotherBean {
        public String c;
        public String a;
        public String b;
    }

    @Test
    @SneakyThrows
    public void testOrderByAlphabetic() {
        MyAnotherBean bean = new MyAnotherBean("c", "a", "b");
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        //language=JSON
        String expectJson = """
                {
                  "a": "a",
                  "b": "b",
                  "c": "c"
                }
                """;
        // 按字母排序
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 压缩一下，移除换行、空格等
        String compressString = expectJson.replaceAll("\\s+", "").trim();
        assertThat(result).isEqualTo(compressString);
    }

}
