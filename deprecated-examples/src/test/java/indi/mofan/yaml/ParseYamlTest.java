package indi.mofan.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;

/**
 * @author mofan
 * @date 2023/7/22 14:38
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParseYamlTest implements WithAssertions {

    private static final File YAML_FILE = new File("./target/test.yaml");

    @Getter
    @Setter
    static class YamlObject {
        private String str;
        private Integer integer;
    }

    @Test
    @Order(1)
    @SneakyThrows
    public void testWrite() {
        ObjectMapper mapper = new YAMLMapper();
        YamlObject object = new YamlObject();
        object.setStr("string");
        object.setInteger(212);
        // 写入 test.yaml 文件中
        mapper.writeValue(YAML_FILE, object);
        assertThat(YAML_FILE).isNotEmpty();
    }

    @Test
    @Order(2)
    @SneakyThrows
    public void testRead() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        YamlObject value = mapper.readValue(YAML_FILE, YamlObject.class);
        assertThat(value).extracting(YamlObject::getStr, YamlObject::getInteger)
                .containsExactly("string", 212);
    }
}
