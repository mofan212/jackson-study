package indi.mofan.advanced;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author mofan
 * @date 2023/12/14 19:06
 * @link <a href="https://www.baeldung.com/jackson-convert-xml-json">Convert XML to JSON Using Jackson</a>
 */
public class ConvertXml2JsonTest implements WithAssertions {

    @Getter
    @Setter
    private static class Flower {
        private String name;
        private Color color;
        private Integer petals;
    }

    public enum Color {PINK, BLUE, YELLOW, RED;}

    // language=XML
   private static final String XML_STRING = """
                <Flower>
                    <name>Poppy</name>
                    <color>RED</color>
                    <petals>9</petals>
                </Flower>""";

    @Test
    @SneakyThrows
    public void testDataBinding() {
        XmlMapper xmlMapper = XmlMapper.builder().build();
        Flower flower = xmlMapper.readValue(XML_STRING, Flower.class);
        assertThat(flower).extracting(Flower::getName, Flower::getColor, Flower::getPetals)
                .containsExactly("Poppy", Color.RED, 9);

        JsonMapper jsonMapper = JsonMapper.builder().build();
        String result = jsonMapper.writeValueAsString(flower);
        String expectJson = """
                {
                  "name": "Poppy",
                  "color": "RED",
                  "petals": 9
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testTreeTraversal() {
        XmlMapper xmlMapper = XmlMapper.builder().build();
        JsonNode jsonNode = xmlMapper.readTree(XML_STRING.getBytes(StandardCharsets.UTF_8));
        JsonMapper jsonMapper = JsonMapper.builder().build();
        String result = jsonMapper.writeValueAsString(jsonNode);
        String expectJson = """
                {
                  "name": "Poppy",
                  "color": "RED",
                  "petals": "9"
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        JsonAssertions.assertThatJson(result).node("petals")
                .isString()
                // 通过 JsonNode 转换得到的无法知晓实际类型
                .isEqualTo("9");
    }
}
