package indi.mofan;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.io.Resources;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

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

    @SneakyThrows
    private void consumeExampleRoot(Consumer<ObjectNode> rootConsumer) {
        URL url = Resources.getResource("example.json");
        try (InputStream stream = url.openStream()) {
            JsonNode tree = mapper.readTree(stream);
            rootConsumer.accept((ObjectNode) tree);
        }
    }

    @Test
    @SneakyThrows
    public void testLocatingANode() {
        consumeExampleRoot(root -> {
            JsonNode lastField = root.path("name").path("last");
            assertThat(lastField.asText()).isEqualTo("Saloranta");
        });
    }

    /**
     * <p>添加的内容：</p>
     * <pre>
     * "address":
     * {
     *     "city": "Seattle",
     *     "state": "Washington",
     *     "country": "United States"
     * }
     * </pre>
     */
    @Test
    public void testAddingANewNode() {
        consumeExampleRoot(root -> {
            root.putObject("address")
                    .put("city", "Seattle")
                    .put("state", "Washington")
                    .put("country", "United States");
            // 存在 address
            JsonNode address = root.path("address");
            assertThat(address.isMissingNode()).isFalse();
            // 断言 address 里的值
            assertThat(address.path("city").textValue()).isEqualTo("Seattle");
            assertThat(address.path("state").textValue()).isEqualTo("Washington");
            assertThat(address.path("country").textValue()).isEqualTo("United States");
        });
    }

    @Test
    @SneakyThrows
    public void testEditingANode() {
        // language=JSON
        String newString = """
                {
                  "nick": "cowtowncoder"
                }""";
        JsonNode newNode = mapper.readTree(newString);
        consumeExampleRoot(root -> {
            root.set("name", newNode);
            JsonNode nameNode = root.path("name");
            assertThat(nameNode.path("first").isMissingNode()).isTrue();
            assertThat(nameNode.path("last").isMissingNode()).isTrue();
            assertThat(nameNode.path("nick").textValue()).isEqualTo("cowtowncoder");
        });
    }

    @Test
    public void testRemovingANode() {
        consumeExampleRoot(root -> {
            root.remove("company");
            assertThat(root.path("company").isMissingNode()).isTrue();
        });
    }

    @SneakyThrows
    private void consumeIteratingRoot(Consumer<ObjectNode> rootConsumer) {
        URL url = Resources.getResource("iterating-example.json");
        try (InputStream stream = url.openStream()) {
            JsonNode tree = mapper.readTree(stream);
            rootConsumer.accept((ObjectNode) tree);
        }
    }

    public String toYaml(JsonNode node) {
        StringBuilder yaml = new StringBuilder();
        processNode(node, yaml, 0);
        return yaml.toString();
    }

    private void processNode(JsonNode jsonNode, StringBuilder yaml, int depth) {
        if (jsonNode.isValueNode()) {
            yaml.append(jsonNode.asText());
        } else if (jsonNode.isArray()) {
            for (JsonNode arrayItem : jsonNode) {
                appendNodeToYaml(arrayItem, yaml, depth, true);
            }
        } else if (jsonNode.isObject()) {
            appendNodeToYaml(jsonNode, yaml, depth, false);
        }
    }

    private void appendNodeToYaml(JsonNode node,
                                  StringBuilder yaml,
                                  int depth,
                                  boolean isArrayItem) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        boolean isFirst = true;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> jsonField = fields.next();
            addFieldNameToYaml(yaml, jsonField.getKey(), depth, isArrayItem && isFirst);
            processNode(jsonField.getValue(), yaml, depth + 1);
            isFirst = false;
        }

    }

    private void addFieldNameToYaml(StringBuilder yaml,
                                    String fieldName,
                                    int depth,
                                    boolean isFirstInArray) {
        if (!yaml.isEmpty()) {
            yaml.append("\n");
            int requiredDepth = (isFirstInArray) ? depth - 1 : depth;
            yaml.append("  ".repeat(Math.max(0, requiredDepth)));
            if (isFirstInArray) {
                yaml.append("- ");
            }
        }
        yaml.append(fieldName);
        yaml.append(": ");
    }

    @Test
    public void testIteratingOverTheNodes() {
        //language=yaml
        String expectYaml = """
                name:
                  first: Tatu
                  last: Saloranta
                title: Jackson founder
                company: FasterXML
                pets:
                  - type: dog
                    number: 1
                  - type: fish
                    number: 50
                """;
        consumeIteratingRoot(root -> {
            String yaml = toYaml(root);
            // 把 YAML 都转成 JSON 来断言
            String result = yaml2Json(yaml);
            JsonAssertions.assertThatJson(result).isEqualTo(yaml2Json(expectYaml));
        });
    }

    @SneakyThrows
    private String yaml2Json(String yaml) {
        YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory());
        Object obj = yamlMapper.readValue(yaml, Object.class);
        return mapper.writeValueAsString(obj);
    }
}
