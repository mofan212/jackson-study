package indi.mofan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import indi.mofan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author mofan 2020/12/14
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JacksonBasicTest implements WithAssertions {

    @Test
    @SneakyThrows
    public void testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        User user = new User("mofan", 18, 177);
        // 实体类解析成 JSON 数据
        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        String expectJson = """
                {
                  "name" : "mofan",
                  "age" : 18,
                  "height" : 177.0
                }
                """;
        JsonAssertions.assertThatJson(string).isEqualTo(expectJson);
        // 从字符串中读取 JSON 数据
        User readValue = mapper.readValue(string, User.class);
        assertThat(readValue).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("mofan", 18, 177.0);
    }

    @Test
    @SneakyThrows
    public void testDeserialization_1() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        URL url = getClass().getClassLoader().getResource("./json/user.json");
        File file = new File(Objects.requireNonNull(url).getFile());
        // 从 File 中读取对象
        User user1 = mapper.readValue(file, User.class);
        assertThat(user1).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("mofan", 18, 178.1);

        String userJson = """
                {
                  "name": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        // 从 String 中读取对象
        User user2 = mapper.readValue(userJson, User.class);
        assertThat(user2).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 20, 177.5);
        // 从 Reader 中读取对象
        User user3 = mapper.readValue(new StringReader(userJson), User.class);
        assertThat(user3).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 20, 177.5);
        // 从 InputStream 中读取对象
        User user4 = mapper.readValue(new FileInputStream(file), User.class);
        assertThat(user4).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("mofan", 18, 178.1);
        // 从字节数组中读取对象
        User user5 = mapper.readValue(userJson.getBytes(StandardCharsets.UTF_8), User.class);
        assertThat(user5).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 20, 177.5);
    }

    @Test
    @SneakyThrows
    public void testDeserialization_2() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String userJson = """
                {
                  "name": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        String jsonArray = """
                [
                  {
                    "name": "mofan"
                  },
                  {
                    "name": "默烦"
                  }
                ]
                """;

        // 从 JSON 数组字符串中读取对象数组
        User[] users1 = mapper.readValue(jsonArray, User[].class);
        assertThat(users1).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly(tuple("mofan", 0, 0.0), tuple("默烦", 0, 0.0));

        // 从 JSON 数组字符串中读取对象列表
        List<User> userList = mapper.readValue(jsonArray, new TypeReference<>() {
        });
        assertThat(userList).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly(tuple("mofan", 0, 0.0), tuple("默烦", 0, 0.0));

        // 从 JSON 数组字符串中读取对象列表
        Map<String, Object> map = mapper.readValue(userJson, new TypeReference<>() {
        });
        assertThat(map).containsAllEntriesOf(Map.of("age", 20, "name", "默烦", "height", 177.5));
    }

    @Test
    @SneakyThrows
    public void testParseJSONIntoJsonNode() {
        String userJson = """
                {
                  "name": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode1 = mapper.readValue(userJson, JsonNode.class);
        assertThat(jsonNode1.get("name").asText()).isEqualTo("默烦");

        JsonNode jsonNode2 = mapper.readTree(userJson);
        assertThat(jsonNode2.get("age").asInt()).isEqualTo(20);

        ObjectNode objectNode = ((ObjectNode) mapper.readTree(userJson));
        objectNode.withObject("/other").put("type", "Student");
        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        String expectJson = """
                {
                  "name" : "默烦",
                  "age" : 20,
                  "height" : 177.5,
                  "other" : {
                    "type" : "Student"
                  }
                }
                """;
        JsonAssertions.assertThatJson(string).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testJsonNodeOperation() {
        String userJson = """
                {
                  "name": "mofan",
                  "age": 20,
                  "hobby": [
                    "music",
                    "game",
                    "study"
                  ],
                  "other": {
                    "type": "Student"
                  }
                }
                                """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(userJson);

        // 获取 name 的字段值
        String name = jsonNode.get("name").asText();
        assertThat(name).isEqualTo("mofan");

        // 获取 age 的字段值
        assertThat(jsonNode.get("age").asInt()).isEqualTo(20);

        // 获取 hobby 数组中第一个值
        String firstHobby = jsonNode.get("hobby").get(0).asText();
        assertThat(firstHobby).isEqualTo("music");

        // 获取嵌套的 JSON 串中 type 的值
        String type = jsonNode.get("other").get("type").asText();
        assertThat(type).isEqualTo("Student");
    }

    @Test
    @SneakyThrows
    public void testAddDataToJSON() {
        String userJson = """
                {
                  "name": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode objectNode = mapper.readValue(userJson, ObjectNode.class);
        // 添加普通字段
        objectNode.put("weight", 65);
        // 再嵌套一个 JSON
        objectNode.withObject("/other").put("type", "Student");
        // 添加一个数组
        objectNode.withArray("hobby").add("music").add("game").add("study");
        // 向数组指定位置添加一个值
        objectNode.withArray("hobby").insert(1, "write");
        // 向数组中嵌套一段 JSON
        objectNode.withArray("jsonArray").add(mapper.readValue(userJson, ObjectNode.class));
        // 向数组中添加一个 Object 节点
        objectNode.withArray("objectArray").addObject().putObject("Test").put("test", "test");
        String expectJson = """
                {
                  "name" : "默烦",
                  "age" : 20,
                  "height" : 177.5,
                  "weight" : 65,
                  "other" : {
                    "type" : "Student"
                  },
                  "hobby" : [ "music", "write", "game", "study" ],
                  "jsonArray" : [ {
                    "name" : "默烦",
                    "age" : 20,
                    "height" : 177.5
                  } ],
                  "objectArray" : [ {
                    "Test" : {
                      "test" : "test"
                    }
                  } ]
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(objectNode)).isEqualTo(expectJson);

        assertThat(objectNode.get("objectArray").get(0).isObject()).isTrue();
        assertThat(objectNode.get("weight").asInt()).isEqualTo(65);
        assertThat(objectNode.get("other").isObject()).isTrue();
        assertThat(objectNode.get("other").get("type").asText()).isEqualTo("Student");
        assertThat(objectNode.get("hobby").get(0).asText()).isEqualTo("music");

        // 删除普通字段
        objectNode.without("age");
        // 删除嵌套 JSON 中的字段
        objectNode.withObject("/other").without("type");
        // 删除嵌套的 JSON，无论内部是否有字段
        objectNode.without("other");
        // 删除数组中某个值
        objectNode.withArray("hobby").remove(0);
        objectNode.withArray("jsonArray").removeAll();

        // 向数组中添加一个 Object
        User user = new User("mofan", 19, 177.5);
        objectNode.withArray("addPojo").addPOJO(user);
        // 向数组中添加带字段的 Object
        objectNode.withArray("addPojoWithField").addObject().putPOJO("user", user);
        expectJson = """
                {
                  "name" : "默烦",
                  "height" : 177.5,
                  "weight" : 65,
                  "hobby" : [ "write", "game", "study" ],
                  "jsonArray" : [ ],
                  "objectArray" : [ {
                    "Test" : {
                      "test" : "test"
                    }
                  } ],
                  "addPojo" : [ {
                    "name" : "mofan",
                    "age" : 19,
                    "height" : 177.5
                  } ],
                  "addPojoWithField" : [ {
                    "user" : {
                      "name" : "mofan",
                      "age" : 19,
                      "height" : 177.5
                    }
                  } ]
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(objectNode)).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testTransform() {
        String userJson = """
                {
                  "name": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // JsonNode 转 Object
        User user = mapper.readValue(userJson, User.class);
        assertThat(user).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 20, 177.5);
        // 或者
        JsonNode jsonNode1 = mapper.readTree(userJson);
        User user1 = mapper.treeToValue(jsonNode1, User.class);
        assertThat(user1).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 20, 177.5);

        // Object 转 JsonNode
        User user2 = new User("mofan", 18, 178.1);
        JsonNode jsonNode2 = mapper.valueToTree(user2);
        String expectJson = """
                {
                  "name" : "mofan",
                  "age" : 18,
                  "height" : 178.1
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(jsonNode2)).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testSerialization_1() {
        User user = new User("默烦", 19, 178.2);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // 写为字符串
        String str = mapper.writeValueAsString(user);
        String expectJson = """
                {
                  "name" : "默烦",
                  "age" : 19,
                  "height" : 178.2
                }
                """;
        JsonAssertions.assertThatJson(str).isEqualTo(expectJson);

        // 写为文件
        File file = new File("./target/test.json");
        mapper.writeValue(file, user);
        assertThat(file).exists();

        // 写为字节流并读取
        byte[] bytes = mapper.writeValueAsBytes(user);
        assertThat(mapper.readValue(bytes, User.class))
                .extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("默烦", 19, 178.2);
    }

    @Test
    @SneakyThrows
    public void testSerialization_2() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> map = new HashMap<>(8);
        map.put("name", "mofan");
        map.put("age", 18);
        map.put("hobby", new String[]{"code", "game", "study"});
        List<String> list = new ArrayList<>();
        list.add("book_1");
        list.add("book_2");
        list.add("book_3");
        map.put("book", list);

        String str = mapper.writeValueAsString(map);
        String expectJson = """
                {
                  "name" : "mofan",
                  "age" : 18,
                  "hobby" : [ "code", "game", "study" ],
                  "book" : [ "book_1", "book_2", "book_3" ]
                }
                """;
        JsonAssertions.assertThatJson(str).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testStreamingParser() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("./target/simpleJSON.json");
        JsonGenerator generator = factory.createGenerator(file, JsonEncoding.UTF8);

        generator.writeStartObject();
        generator.writeStringField("msg", "Hello Jackson");
        generator.writeEndObject();
        generator.close();

        JsonParser parser = factory.createParser(file);
        JsonToken jsonToken = parser.nextToken();

        jsonToken = parser.nextToken();
        assertThat(parser.getCurrentName()).isEqualTo("msg");
        assertThat(jsonToken).isEqualTo(JsonToken.FIELD_NAME);

        jsonToken = parser.nextToken();
        assertThat(jsonToken).isEqualTo(JsonToken.VALUE_STRING);

        String msg = parser.getText();
        assertThat(msg).isEqualTo("Hello Jackson");
        parser.close();
    }

    @Test
    @Order(1)
    @SneakyThrows
    public void testJsonGenerator() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("./target/testJsonGenerator.json");

        try (JsonGenerator generator = factory.createGenerator(file, JsonEncoding.UTF8)) {
            // start {
            generator.writeStartObject();
            // "name" : "默烦"
            generator.writeStringField("name", "默烦");
            // "age" : 18
            generator.writeNumberField("age", 18);
            // "isGirl" : false
            generator.writeBooleanField("isGirl", false);
            // "hobby" : ["code", "study"]
            generator.writeFieldName("hobby");
            // [
            generator.writeStartArray();
            // code, study
            generator.writeString("code");
            generator.writeString("study");
            // ]
            generator.writeEndArray();
            // end }
            generator.writeEndObject();
        }

        // 数据写入结束，来读取一下
        Map<String, Object> map = mapper.readValue(file, new TypeReference<>() {
        });
        assertThat(map).containsAllEntriesOf(Map.of(
                "age", 18,
                "hobby", List.of("code", "study"),
                "isGirl", false,
                "name", "默烦"));
    }

    @Test
    @Order(2)
    @SneakyThrows
    public void testGetToken() {
        JsonFactory jsonFactory = new JsonFactory();
        // 先执行 testJsonGenerator() 测试方法，确保存在 testJsonGenerator.json 文件
        File file = new File("./target/testJsonGenerator.json");
        JsonParser parser = jsonFactory.createParser(file);

        List<JsonToken> list = new ArrayList<>();
        while (!parser.isClosed()) {
            list.add(parser.nextToken());
        }
        assertThat(list).containsExactly(
                JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_STRING,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_FALSE,
                JsonToken.FIELD_NAME,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_STRING,
                JsonToken.VALUE_STRING,
                JsonToken.END_ARRAY,
                JsonToken.END_OBJECT,
                null
        );
    }

    @Test
    @Order(3)
    @SneakyThrows
    public void testJsonParser() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("./target/testJsonGenerator.json");
        JsonParser parser = factory.createParser(file);
        while (!parser.isClosed()) {
            if (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
                String fieldName = parser.getCurrentName();

                // 移动到下一标记处
                parser.nextToken();
                if ("name".equals(fieldName)) {
                    assertThat(parser.getValueAsString()).isEqualTo("默烦");
                }
                if ("age".equals(fieldName)) {
                    assertThat(parser.getValueAsInt()).isEqualTo(18);
                }
                if ("isGirl".equals(fieldName)) {
                    assertThat(parser.getValueAsBoolean()).isFalse();
                }
                if ("hobby".equals(fieldName)) {
                    List<String> list = new ArrayList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        list.add(parser.getValueAsString());
                    }
                    assertThat(list).containsExactly("code", "study");
                }
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Employee {
        @JsonProperty(value = "employeeName", index = 2)
        private String name;
        @JsonProperty(index = 1)
        private int age;
        @JsonProperty(index = 0)
        private double height;
    }

    @Test
    @SneakyThrows
    public void testAnnotation() {
        ObjectMapper mapper = new ObjectMapper();
        Employee employee = new Employee("mofan", 19, 178.2);
        // 序列化
        LinkedHashMap<String, Object> map = mapper.convertValue(employee, new TypeReference<>() {
        });
        // 转成 LinkedHashMap 进行比较
        Map<String, Object> expectMap = new LinkedHashMap<>();
        expectMap.put("height", 178.2);
        expectMap.put("age", 19);
        expectMap.put("employeeName", "mofan");
        assertThat(map).containsExactlyEntriesOf(expectMap);
        // 反序列化
        String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        Employee readValue = mapper.readValue(str, Employee.class);
        assertThat(readValue).extracting(Employee::getName, Employee::getAge, Employee::getHeight)
                .containsExactly("mofan", 19, 178.2);
        //language=JSON
        String userJson = """
                {
                  "employeeName": "默烦",
                  "age": 20,
                  "height": 177.5
                }
                """;
        Employee value = mapper.readValue(userJson, Employee.class);
        assertThat(value).extracting(Employee::getName, Employee::getAge, Employee::getHeight)
                .containsExactly("默烦", 20, 177.5);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonRootName("root")
    @JsonPropertyOrder({"decimal", "string", "integer"})
    static class Root {
        private String string;
        private Integer integer;
        private Double decimal;
    }

    @Test
    @SneakyThrows
    public void testSerializationAnnotation() {
        // 使用 @JsonRootName 注解后，必须启用 SerializationFeature.WRAP_ROOT_VALUE
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
        Root root = new Root("str", 212, 3.14);
        String json = mapper.writeValueAsString(root);
        //language=JSON
        String expectJson = """
                {
                  "root": {
                    "decimal": 3.14,
                    "string": "str",
                    "integer": 212
                  }
                }
                """;
        JsonAssertions.assertThatJson(json).isEqualTo(expectJson);
        // 转成 LinkedHashMap 方便比较顺序（此时最外层不再有 root 根属性）
        LinkedHashMap<String, Object> map = mapper.convertValue(root, new TypeReference<>() {
        });
        Map<String, Object> expectMap = new LinkedHashMap<>();
        expectMap.put("decimal", 3.14);
        expectMap.put("string", "str");
        expectMap.put("integer", 212);
        assertThat(map).asInstanceOf(MAP)
                .doesNotContainKey("root")
                .hasSize(3)
                .containsExactlyEntriesOf(expectMap);
    }

    @Test
    @SneakyThrows
    public void testJsonCreator() {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {
                  "stuName": "默烦",
                  "age": 20,
                  "height": 177.5,
                  "school": {
                    "name": "Test"
                  }
                }
                """;
        Student student = mapper.readValue(json, Student.class);
        assertThat(student).extracting(
                Student::stuName, Student::age,
                Student::height, i -> i.school().name()
        ).containsExactly("默烦", 20, 177.5, "Test");
    }

    @FieldNameConstants
    record Student(String stuName, int age, double height, JacksonBasicTest.Student.School school) {
        @JsonCreator
        Student(@JsonProperty(Fields.stuName) String stuName,
                @JsonProperty(Fields.age) int age,
                @JsonProperty(Fields.height) double height,
                @JsonProperty(Fields.school) School school) {
            this.stuName = stuName;
            this.age = age;
            this.height = height;
            this.school = school;
        }

        @FieldNameConstants
        record School(String name) {
            @JsonCreator
            School(@JsonProperty(Fields.name) String name) {
                this.name = name;
            }
        }
    }

    @Test
    @SneakyThrows
    public void testConstructorProperties() {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {
                  "name": "mofan",
                  "personAge": 20
                }
                """;
        Person person = mapper.readValue(json, Person.class);
        assertThat(person).extracting(Person::getPersonName, Person::getPersonAge)
                .containsExactly("mofan", 20);
    }

    @Getter
    static class Person {
        private final String personName;
        private final int personAge;

        @ConstructorProperties({"name", "personAge"})
        public Person(String name, int personAge) {
            this.personName = name;
            this.personAge = personAge;
        }
    }

    @Test
    @SneakyThrows
    public void testConversions() {
        ObjectMapper mapper = new ObjectMapper();
        // List<Integer> to int[]
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        assertThat(list).containsExactly(1, 2, 3);
        int[] ints = mapper.convertValue(list, int[].class);
        assertThat(ints).containsExactly(1, 2, 3);
        // POJO to Map
        User user = new User("mofan", 19, 178.3);
        Map<String, Object> map = mapper.convertValue(user, new TypeReference<>() {
        });
        assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of(
                "age", 19,
                "name", "mofan",
                "height", 178.3
        ));
        // Map to POJO
        User pojo = mapper.convertValue(map, User.class);
        assertThat(pojo).extracting(User::getName, User::getAge, User::getHeight)
                .containsExactly("mofan", 19, 178.3);
        // decode Base64
        String base64 = "bW9mYW4=";
        byte[] bytes = mapper.convertValue(base64, byte[].class);
        String decode = new String(bytes, StandardCharsets.UTF_8);
        assertThat(decode).isEqualTo("mofan");

        // POJO 转 JsonNode
        User people = new User("默烦", 19, 177.5);
        JsonNode jsonNode = mapper.convertValue(people, JsonNode.class);
        String expectJson = """
                {
                  "name": "默烦",
                  "age": 19,
                  "height": 177.5
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(jsonNode)).isEqualTo(expectJson);
    }

    @Test
    @SneakyThrows
    public void testLongValue() {
        String withLong = """
                {
                  "id1": "98765432112345",
                  "id2": "98765432112345L",
                  "id3": 98765432112345,
                  "id4": "1001",
                  "id5": "1001L",
                  "id6": 1001
                }
                """;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = ((ObjectNode) mapper.readTree(withLong));
        JsonNode id1 = objectNode.get("id1");
        assertThat(id1.isLong()).isFalse();
        JsonNode id2 = objectNode.get("id2");
        assertThat(id2.isLong()).isFalse();
        JsonNode id3 = objectNode.get("id3");
        assertThat(id3.isLong()).isTrue();
        JsonNode id4 = objectNode.get("id4");
        assertThat(id4.isLong()).isFalse();
        JsonNode id5 = objectNode.get("id5");
        assertThat(id5.isLong()).isFalse();
        JsonNode id6 = objectNode.get("id6");
        assertThat(id6.isLong()).isFalse();
    }
}
