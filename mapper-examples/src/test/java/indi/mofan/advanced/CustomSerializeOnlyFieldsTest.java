package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/10 15:36
 * @link <a href="https://www.baeldung.com/jackson-serialize-field-custom-criteria">Serialize Only Fields That Meet a Custom Criteria With Jackson</a>
 */
public class CustomSerializeOnlyFieldsTest implements WithAssertions {

    @JsonFilter("myFilter")
    private static class MyDto {
        private int intValue;

        public MyDto() {
            super();
        }

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }
    }

    @Test
    @SneakyThrows
    public void testUseFilterControlSerialization() {
        PropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator generator,
                                         SerializerProvider provider, PropertyWriter writer) throws Exception {
                if (include(writer)) {
                    // 不叫 intValue 直接序列化
                    if (!writer.getName().equals("intValue")) {
                        writer.serializeAsField(pojo, generator, provider);
                        return;
                    }
                    // 叫 intValue 的，要不小于 0 才被序列化
                    int intValue = ((MyDto) pojo).getIntValue();
                    if (intValue >= 0) {
                        writer.serializeAsField(pojo, generator, provider);
                    }
                } else if (!generator.canOmitFields()) {
                    writer.serializeAsOmittedField(pojo, generator, provider);
                }
            }
        };

        SimpleFilterProvider filters = new SimpleFilterProvider().addFilter("myFilter", filter);
        MyDto myDto = new MyDto();
        myDto.setIntValue(-1);

        JsonMapper mapper = JsonMapper.builder()
                .filterProvider(filters)
                .build();
        String result = mapper.writeValueAsString(myDto);
        // 空 JSON 对象
        assertThat(result).isEqualTo("{}");
    }

    @JsonIgnoreProperties("hidden")
    private interface Hidable {
        boolean isHidden();
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Address implements Hidable {
        private String city;
        private String country;
        private boolean hidden;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Person implements Hidable {
        private String name;
        private Address address;
        private boolean hidden;
    }

    private static class HidableSerializer extends JsonSerializer<Hidable> {

        private final JsonSerializer<Object> defaultSerializer;

        public HidableSerializer(JsonSerializer<Object> serializer) {
            defaultSerializer = serializer;
        }

        @Override
        public void serialize(Hidable value,
                              JsonGenerator jgen,
                              SerializerProvider provider) throws IOException {
            // 如果需要被隐藏，则字节返回
            if (value.isHidden()) {
                return;
            }
            defaultSerializer.serialize(value, jgen, provider);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Hidable value) {
            return (value == null || value.isHidden());
        }
    }

    @Test
    @SneakyThrows
    public void testSkipObjectsConditionally() {
        SimpleModule module = new SimpleModule() {
            @Serial
            private static final long serialVersionUID = -3907645418456418540L;

            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                              BeanDescription beanDesc,
                                                              JsonSerializer<?> serializer) {
                        if (Hidable.class.isAssignableFrom(beanDesc.getBeanClass())) {
                            return new HidableSerializer((JsonSerializer<Object>) serializer);
                        }
                        return serializer;
                    }
                });
            }
        };

        JsonMapper mapper = JsonMapper.builder()
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .addModule(module)
                .build();

        Address ad1 = new Address("tokyo", "jp", true);
        Address ad2 = new Address("london", "uk", false);
        Address ad3 = new Address("ny", "usa", false);
        Person p1 = new Person("john", ad1, false);
        Person p2 = new Person("tom", ad2, true);
        Person p3 = new Person("adam", ad3, false);
        String result = mapper.writeValueAsString(List.of(p1, p2, p3));
        //language=JSON
        String expectJson = """
                [
                  {
                    "name": "john"
                  },
                  {
                    "name": "adam",
                    "address": {
                      "city": "ny",
                      "country": "usa"
                    }
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
