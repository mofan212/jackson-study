package indi.mofan;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author mofan
 * @date 2023/12/25 10:20
 * @link <a href="https://www.baeldung.com/jackson-json-node-tree-model">Working with Tree Model Nodes in Jackson</a>
 */
public class TreeModelNodesTest implements WithAssertions {

    /**
     * 创建 Mapper 是昂贵的，共用一个
     */
    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class NodeBean {
        private int id;
        private String name;
    }

    @Test
    public void testCreateANode() {
        // 构造 node 对象
        ObjectNode objectNode = mapper.createObjectNode();
        assertThat(objectNode.isEmpty()).isTrue();
        // or
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        assertThat(node.isEmpty()).isTrue();

        NodeBean fromValue = new NodeBean(2020, "mofan212.github.io");
        // valueToTree
        JsonNode jsonNode = mapper.valueToTree(fromValue);
        assertThat(jsonNode.get("id").intValue()).isEqualTo(2020);
        // convertValue
        jsonNode = mapper.convertValue(fromValue, JsonNode.class);
        assertThat(jsonNode.get("name").textValue()).isEqualTo("mofan212.github.io");
    }

    @Test
    @SneakyThrows
    public void testWriteOutAsJson() {
        // language=JSON
        String newString = """
                {
                  "nick": "mofan"
                }""";
        JsonNode jsonNode = mapper.readTree(newString);
        ObjectNode objectNode = mapper.createObjectNode();

        // name -> nick -> mofan
        objectNode.set("name", jsonNode);

        JsonNode node = objectNode.path("name").path("nick");
        assertThat(node.isMissingNode()).isFalse();
        assertThat(node.textValue()).isEqualTo("mofan");
    }

    @Test
    @SneakyThrows
    public void testConvert2Object() {
        // language=JSON
        String inputJson = """
                {
                  "name": "mofan"
                }
                """;
        JsonNode jsonNode = mapper.readTree(inputJson);

        NodeBean toValue = mapper.treeToValue(jsonNode, NodeBean.class);
        assertThat(toValue).extracting(NodeBean::getName).isEqualTo("mofan");

        NodeBean convertValue = mapper.convertValue(jsonNode, NodeBean.class);
        assertThat(convertValue).extracting(NodeBean::getName).isEqualTo("mofan");

        try (JsonParser jsonParser = mapper.treeAsTokens(jsonNode)) {
            NodeBean nodeBean = mapper.readValue(jsonParser, NodeBean.class);
            assertThat(nodeBean).extracting(NodeBean::getName).isEqualTo("mofan");
        }
    }
}
