package indi.mofan.more;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
 * @date 2024/1/4 11:14
 */
public class JsonNamingTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    private static class NamingBean {
        private int id;
        private String beanName;

        private static NamingBean buildDefault() {
            NamingBean bean = new NamingBean();
            bean.setId(3);
            bean.setBeanName("Naming Bean");
            return bean;
        }
    }

    @Test
    @SneakyThrows
    public void testDefault() {
        NamingBean bean = NamingBean.buildDefault();
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 3,
                  "beanName": "Naming Bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
    private static class KebabCaseMixIn {
    }

    @Test
    @SneakyThrows
    public void testKebabCase() {
        NamingBean bean = NamingBean.buildDefault();
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(NamingBean.class, KebabCaseMixIn.class)
                .build();
        String result = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 3,
                  "bean-name": "Naming Bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)
    private static class LowerCaseMixIn {
    }

    @Test
    @SneakyThrows
    public void testLowerCase() {
        NamingBean bean = NamingBean.buildDefault();
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(NamingBean.class, LowerCaseMixIn.class)
                .build();
        String result = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 3,
                  "beanname": "Naming Bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private static class SnakeCaseMixIn {
    }

    @Test
    @SneakyThrows
    public void testSnakeCase() {
        NamingBean bean = NamingBean.buildDefault();
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(NamingBean.class, SnakeCaseMixIn.class)
                .build();
        String result = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "id": 3,
                  "bean_name": "Naming Bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    private static class UpperCamelCaseMixIn {
    }

    @Test
    @SneakyThrows
    public void testUpperCamelCase() {
        NamingBean bean = NamingBean.buildDefault();
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(NamingBean.class, UpperCamelCaseMixIn.class)
                .build();
        String result = mapper.writeValueAsString(bean);
        // language=JSON
        String expectJson = """
                {
                  "Id": 3,
                  "BeanName": "Naming Bean"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
