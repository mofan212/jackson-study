package indi.mofan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

/**
 * @author mofan
 * @date 2023/12/21 11:19
 * @link <a href="https://www.baeldung.com/jackson-compare-two-json-objects">Compare Two JSON Objects with Jackson</a>
 */
public class CompareTwoJsonObjectsTest implements WithAssertions {
    @Test
    @SneakyThrows
    public void testCompareTwoSimpleJsonObjects() {
        // language=JSON
        String s1 = """
                {
                  "employee": {
                    "id": "1212",
                    "fullName": "John Miles",
                    "age": 34
                  }
                }
                """;
        // language=JSON
        String s2 = """
                {
                  "employee": {
                    "id": "1212",
                    "age": 34,
                    "fullName": "John Miles"
                  }
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();
        // 使用 JsonNode 进行比较
        JsonNode jsonNode1 = mapper.readTree(s1);
        JsonNode jsonNode2 = mapper.readTree(s2);
        // 尽管顺序不同，但比较时会忽略顺序
        assertThat(jsonNode1).isEqualTo(jsonNode2);
    }

    @Test
    @SneakyThrows
    public void testCompareWithNestedElements() {
        // language=JSON
        String s1 = """
                {
                  "employee": {
                    "id": "1212",
                    "fullName": "John Miles",
                    "age": 34,
                    "contact": {
                      "email": "john@xyz.com",
                      "phone": "9999999999"
                    }
                  }
                }
                """;
        // language=JSON
        String s2 = """
                {
                  "employee": {
                    "id": "1212",
                    "age": 34,
                    "fullName": "John Miles",
                    "contact": {
                      "email": "john@xyz.com",
                      "phone": "9999999999"
                    }
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode jsonNode1 = mapper.readTree(s1);
        JsonNode jsonNode2 = mapper.readTree(s2);
        // 嵌套元素也能比较
        assertThat(jsonNode1).isEqualTo(jsonNode2);
    }

    @Test
    @SneakyThrows
    public void testCompareContainingAListElement() {
        // language=JSON
        String s1 = """
                {
                  "employee": {
                    "id": "1212",
                    "fullName": "John Miles",
                    "age": 34,
                    "skills": [
                      "Java",
                      "C++",
                      "Python"
                    ]
                  }
                }
                """;
        // language=JSON
        String s2 = """
                {
                  "employee": {
                    "id": "1212",
                    "age": 34,
                    "fullName": "John Miles",
                    "skills": [
                      "Java",
                      "C++",
                      "Python"
                    ]
                  }
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode jsonNode1 = mapper.readTree(s1);
        JsonNode jsonNode2 = mapper.readTree(s2);
        // 只有当列表元素顺序一样时，它们才完全相等
        assertThat(jsonNode1).isEqualTo(jsonNode2);
    }

    private static class NumericNodeComparator implements Comparator<JsonNode> {
        @Override
        @SuppressWarnings("ComparatorMethodParameterNotUsed")
        public int compare(JsonNode o1, JsonNode o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            if ((o1 instanceof NumericNode) && (o2 instanceof NumericNode)) {
                Double d1 = o1.asDouble();
                Double d2 = o2.asDouble();
                if (d1.compareTo(d2) == 0) {
                    return 0;
                }
            }
            return 1;
        }
    }

    @Test
    @SneakyThrows
    public void testCustomComparator2CompareNumericValues() {
        // language=JSON
        String s1 = """
                {
                  "name": "John",
                  "score": 5.0
                }
                """;
        // language=JSON
        String s2 = """
                {
                  "name": "John",
                  "score": 5
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode jsonNode1 = mapper.readTree(s1);
        JsonNode jsonNode2 = mapper.readTree(s2);
        // score 值不相等，默认比较器下，s1 与 s2 不相等
        assertThat(jsonNode1).isNotEqualTo(jsonNode2);
        // 使用自定义的比较器进行比较
        NumericNodeComparator comparator = new NumericNodeComparator();
        assertThat(jsonNode1.equals(comparator, jsonNode2)).isTrue();
    }

    private static class TextNodeComparator implements Comparator<JsonNode> {
        @Override
        @SuppressWarnings("ComparatorMethodParameterNotUsed")
        public int compare(JsonNode o1, JsonNode o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            if ((o1 instanceof TextNode) && (o2 instanceof TextNode)) {
                String s1 = o1.asText();
                String s2 = o2.asText();
                if (s1.equalsIgnoreCase(s2)) {
                    return 0;
                }
            }
            return 1;
        }
    }

    @Test
    @SneakyThrows
    public void testCustomComparator2CompareTextValues() {
        // language=JSON
        String s1 = """
                {
                  "name": "john",
                  "score": 5
                }
                """;
        // language=JSON
        String s2 = """
                {
                  "name": "JOHN",
                  "score": 5
                }
                """;
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode jsonNode1 = mapper.readTree(s1);
        JsonNode jsonNode2 = mapper.readTree(s2);
        TextNodeComparator comparator = new TextNodeComparator();
        // 对文本忽略大小写进行比较
        assertThat(jsonNode1.equals(comparator, jsonNode2)).isTrue();
    }
}
