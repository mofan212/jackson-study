package indi.mofan.serialization;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/1 10:38
 * @link <a href="https://www.baeldung.com/jackson-annotations#5-jsonvalue">JsonValue</a>
 */
public class JsonValueTest implements WithAssertions {

    public enum TypeEnumWithValue {

        TYPE1(1, "Type A"),

        TYPE2(2, "Type 2");

        @Getter
        private final Integer id;
        private final String name;

        TypeEnumWithValue(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }

    @Test
    @SneakyThrows
    public void testJsonValue() {
        JsonMapper mapper = JsonMapper.builder().build();
        String enumAsString = mapper.writeValueAsString(TypeEnumWithValue.TYPE1);
        //  @JsonValue 表示使用一个方法来序列化整个实例
        assertThat(enumAsString).isEqualTo("\"Type A\"");
    }
}
