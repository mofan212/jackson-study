package indi.mofan.advanced;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/20 10:47
 * @link <a href="https://www.baeldung.com/jackson-collection-array">Jackson – Unmarshall to Collection/Array</a>
 */
public class Unmarshall2CollectionTest implements WithAssertions {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MyDto {
        private String stringValue;
        private int intValue;
        private boolean booleanValue;
    }

    @Test
    @SneakyThrows
    public void testUnmarshall2Array() {
        List<MyDto> list = List.of(
                new MyDto("a", 1, true), new MyDto("bc", 3, false)
        );
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(list);
        // language=JSON
        String expectJson = """
                [
                  {
                    "stringValue": "a",
                    "intValue": 1,
                    "booleanValue": true
                  },
                  {
                    "stringValue": "bc",
                    "intValue": 3,
                    "booleanValue": false
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        // 往数组反序列化
        MyDto[] asArray = mapper.readValue(result, MyDto[].class);
        assertThat(asArray).extracting(MyDto::getStringValue)
                .containsExactly("a", "bc");
    }

    @Test
    @SneakyThrows
    public void testUnmarshall2Collection() {
        List<MyDto> list = List.of(
                new MyDto("a", 1, true), new MyDto("bc", 3, false)
        );
        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(list);
        // language=JSON
        String expectJson = """
                [
                  {
                    "stringValue": "a",
                    "intValue": 1,
                    "booleanValue": true
                  },
                  {
                    "stringValue": "bc",
                    "intValue": 3,
                    "booleanValue": false
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        @SuppressWarnings("unchecked")
        List<MyDto> asList = mapper.readValue(result, List.class);
        // 这种情况下，反序列化出的数据是 LinkedHashMap 实例
        assertThat(asList).element(0).isInstanceOf(LinkedHashMap.class);

        // with TypeReference help
        asList = mapper.readValue(result, new TypeReference<>() {
        });
        assertThat(asList).element(0).isInstanceOf(MyDto.class);

        // readValue accepts a JavaType
        CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, MyDto.class);
        // 使用这种方式时，MyDto 中需要有默认无参构造器
        asList = mapper.readValue(result, javaType);
        assertThat(asList).element(0).isInstanceOf(MyDto.class);
    }
}
