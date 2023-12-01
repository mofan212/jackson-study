package indi.mofan.deserialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 14:09
 * @link <a href="https://www.baeldung.com/jackson-annotations#1-jsoncreator">JsonCreator</a>
 */
public class JsonCreatorTest implements WithAssertions {
    //language=JSON
    public static final String TARGET_JSON = """
            {
              "id": 1,
              "theName": "My bean"
            }
            """;

    @Getter
    static class BeanWithCreator {
        public int id;
        public String name;

        @JsonCreator
        public static BeanWithCreator from(@JsonProperty("id") int id,
                                           @JsonProperty("theName") String name) {
            BeanWithCreator bean = new BeanWithCreator();
            bean.id = id;
            bean.name = name;
            return bean;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonCreator() {
        JsonMapper mapper = JsonMapper.builder().build();
        /*
         * 使用 @JsonCreator 指定反序列化时所使用的构造方法或工厂方法，
         * 它经常与 @JsonProperty 一起使用
         */
        BeanWithCreator bean = mapper.readValue(TARGET_JSON, BeanWithCreator.class);
        assertThat(bean).isNotNull()
                .extracting(BeanWithCreator::getId, BeanWithCreator::getName)
                .containsExactly(1, "My bean");
    }
}
