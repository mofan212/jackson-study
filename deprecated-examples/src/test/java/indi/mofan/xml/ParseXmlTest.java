package indi.mofan.xml;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;

/**
 * @author mofan
 * @date 2023/7/22 15:19
 */
public class ParseXmlTest implements WithAssertions {

    private static final File XML_FILE = new File("./target/test.xml");

    @Getter
    @Setter
    static class XmlObject {
        private String string;
        private Integer integer;
    }

    @Test
    @SneakyThrows
    public void testWrite() {
        ObjectMapper mapper = new XmlMapper();
        XmlObject object = new XmlObject();
        object.setString("string");
        object.setInteger(212);
        mapper.writeValue(XML_FILE, object);
        assertThat(XML_FILE).isNotEmpty();
    }

    @Test
    @SneakyThrows
    public void testRead() {
        ObjectMapper mapper = XmlMapper.builder().build();
        XmlObject value = mapper.readValue(XML_FILE, XmlObject.class);
        assertThat(value).extracting(XmlObject::getString, XmlObject::getInteger)
                .containsExactly("string", 212);
    }

    @Getter
    @Setter
    @JacksonXmlRootElement(localName = "Root")
    static class AnnotationTestObject {
        private String string;
        private Integer integer;
        private Double decimal;
        @JacksonXmlText
        private String str;
        @JacksonXmlCData
        private String code;
        @JacksonXmlProperty(isAttribute = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date date;
    }

    @Test
    @SneakyThrows
    public void testAnnotation() {
        AnnotationTestObject object = new AnnotationTestObject();
        object.setString("string");
        object.setInteger(212);
        object.setDecimal(3.14);
        object.setStr("str");
        object.setCode("hello world!");
        object.setDate(new Date(1672502400000L));

        XmlMapper mapper = XmlMapper.builder()
                // 格式化输出
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        File file = new File("./target/xml-annotation.xml");
        mapper.writeValue(file, object);
        assertThat(file).isNotEmpty();
    }
}
