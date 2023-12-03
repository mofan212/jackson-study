package indi.mofan.custom;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/3 16:13
 * @link <a href="https://www.baeldung.com/jackson-annotations#custom-jackson-annotation">Custom Jackson Annotation</a>
 */
public class CustomAnnotationTest implements WithAssertions {

    @CustomAnnotation
    @AllArgsConstructor
    private static class BeanWithCustomAnnotation {
        public int id;
        public String name;
        public Date dateCreated;
    }

    @Test
    @SneakyThrows
    public void testCustomAnnotation() {
        BeanWithCustomAnnotation bean
                = new BeanWithCustomAnnotation(1, "My bean", null);
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        // 只包含非 null 的数据，序列化后的字段按照 name、id、dateCreated 进行排序
        String expectString = "{\"name\":\"My bean\",\"id\":1}";
        assertThat(result).isEqualTo(expectString);
    }
}
