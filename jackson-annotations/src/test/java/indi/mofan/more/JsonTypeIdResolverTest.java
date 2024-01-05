package indi.mofan.more;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mofan
 * @date 2024/1/5 16:22
 */
public class JsonTypeIdResolverTest implements WithAssertions {

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "@type"
    )
    @JsonTypeIdResolver(BeanIdResolver.class)
    private static class AbstractBean {
        private int id;

        protected AbstractBean(int id) {
            this.id = id;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class FirstBean extends AbstractBean {
        String firstName;

        public FirstBean(int id, String name) {
            super(id);
            setFirstName(name);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class LastBean extends AbstractBean {
        String lastName;

        public LastBean(int id, String name) {
            super(id);
            setLastName(name);
        }
    }

    @Getter
    @Setter
    private static class BeanContainer {
        private List<AbstractBean> beans;
    }

    private static class BeanIdResolver extends TypeIdResolverBase {

        private JavaType superType;

        @Override
        public void init(JavaType baseType) {
            superType = baseType;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.NAME;
        }

        @Override
        public String idFromValue(Object obj) {
            return idFromValueAndType(obj, obj.getClass());
        }

        @Override
        public String idFromValueAndType(Object obj, Class<?> subType) {
            return switch (subType.getSimpleName()) {
                case "FirstBean" -> "bean1";
                case "LastBean" -> "bean2";
                default -> null;
            };
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            Class<?> subType = switch (id) {
                case "bean1" -> FirstBean.class;
                case "bean2" -> LastBean.class;
                default -> null;
            };
            return context.constructSpecializedType(superType, subType);
        }
    }

    @Test
    @SneakyThrows
    public void test() {
        FirstBean bean1 = new FirstBean(1, "Bean 1");
        LastBean bean2 = new LastBean(2, "Bean 2");

        List<AbstractBean> beans = new ArrayList<>();
        beans.add(bean1);
        beans.add(bean2);

        BeanContainer serializedContainer = new BeanContainer();
        serializedContainer.setBeans(beans);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(serializedContainer);
        // language=JSON
        String expectJson = """
                {
                  "beans": [
                    {
                      "@type": "bean1",
                      "id": 1,
                      "firstName": "Bean 1"
                    },
                    {
                      "@type": "bean2",
                      "id": 2,
                      "lastName": "Bean 2"
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        BeanContainer value = mapper.readValue(expectJson, BeanContainer.class);
        assertThat(value.getBeans()).hasSize(2)
                .map(i -> i.getClass().getSimpleName())
                .containsExactly(FirstBean.class.getSimpleName(), LastBean.class.getSimpleName());
    }
}
