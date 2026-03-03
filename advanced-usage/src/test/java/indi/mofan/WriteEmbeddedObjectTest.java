package indi.mofan;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

/**
 * DTO 转 Map，特定类型的对象保留引用，而不序列化为 Map
 *
 * @author mofan
 * @date 2026/3/3 14:27
 */
public class WriteEmbeddedObjectTest implements WithAssertions {

    @Getter
    @Setter
    static class SpecificType {
        private String data;

        public SpecificType(String data) {
            this.data = data;
        }
    }

    @Getter
    @Setter
    @FieldNameConstants
    static class InnerDTO {
        private String innerName;
        private SpecificType specificObj;
    }

    @Getter
    @Setter
    @FieldNameConstants
    static class OuterDTO {
        private Integer id;
        private InnerDTO innerDto;
    }

    static final JsonMapper mapper;

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(SpecificType.class, new JsonSerializer<>() {
            @Override
            public void serialize(SpecificType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                // 将原对象作为「嵌入对象」直接写入，Jackson 在转换为 Map 时会原样取出该引用
                gen.writeEmbeddedObject(value);
            }
        });
        mapper = JsonMapper.builder()
                .addModule(module)
                .build();
    }

    @Test
    public void testWriteEmbeddedObject() {
        SpecificType targetObject = new SpecificType("Sensitive Data");

        InnerDTO inner = new InnerDTO();
        inner.setInnerName("Inner");
        inner.setSpecificObj(targetObject);

        OuterDTO outer = new OuterDTO();
        outer.setId(1);
        outer.setInnerDto(inner);

        Map<String, Object> map = mapper.convertValue(outer, new TypeReference<>() {
        });

        assertThat(map).extractingByKey(OuterDTO.Fields.innerDto, MAP)
                .extractingByKey(InnerDTO.Fields.specificObj)
                .isSameAs(targetObject);
    }
}
