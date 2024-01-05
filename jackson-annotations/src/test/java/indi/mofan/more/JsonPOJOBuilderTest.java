package indi.mofan.more;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2024/1/5 15:44
 */
public class JsonPOJOBuilderTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonDeserialize(builder = BeanBuilder.class)
    private static class POJOBuilderBean {
        private int identity;
        private String beanName;
    }

    @JsonPOJOBuilder(buildMethodName = "createBean", withPrefix = "construct")
    private static class BeanBuilder {
        private int idValue;
        private String nameValue;

        public BeanBuilder constructId(int id) {
            this.idValue = id;
            return this;
        }

        public BeanBuilder constructName(String name) {
            this.nameValue = name;
            return this;
        }

        public POJOBuilderBean createBean() {
            return new POJOBuilderBean(this.idValue, this.nameValue);
        }
    }

    @Test
    @SneakyThrows
    public void test() {
        // language=JSON
        String inputJson = """
                {
                  "id": 5,
                  "name": "POJO Builder Bean"
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();
        POJOBuilderBean bean = mapper.readValue(inputJson, POJOBuilderBean.class);
        assertThat(bean).extracting(POJOBuilderBean::getIdentity, POJOBuilderBean::getBeanName)
                .containsExactly(5, "POJO Builder Bean");
    }
}
