package indi.mofan.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mofan 2020/12/14
 */
public class JacksonBasicTest {

    @Test
    @SneakyThrows
    public void testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        User user = new User("mofan", 18, 177);
        // 实体类解析成 JSON 数据
        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        System.out.println(string);
        // 从字符串中读取 JSON 数据
        User readValue = mapper.readValue(string, User.class);
        System.out.println(readValue);
    }

    @Test
    @SneakyThrows
    public void testDeserialization_1() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        File file = new File("src\\main\\java\\indi\\mofan\\json\\user.json");
        // 从 File 中读取对象
        User user1 = mapper.readValue(file, User.class);
        System.out.println(user1);

        String userJson = "{\"name\":\"默烦\",\"age\":20,\"height\":177.5}";
        // 从 String 中读取对象
        User user2 = mapper.readValue(userJson, User.class);
        System.out.println(user2);
        // 从 Reader 中读取对象
        User user3 = mapper.readValue(new StringReader(userJson), User.class);
        System.out.println(user3);
        // 从 InputStream 中读取对象
        User user4 = mapper.readValue(new FileInputStream(file), User.class);
        System.out.println(user4);
        // 从字节数组中读取对象
        User user5 = mapper.readValue(userJson.getBytes(StandardCharsets.UTF_8), User.class);
        System.out.println(user5);
    }

    @Test
    @SneakyThrows
    public void testDeserialization_2() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String userJson = "{\"name\":\"默烦\",\"age\":20,\"height\":177.5}";
        String jsonArray = "[{\"name\":\"mofan\"}, {\"name\":\"默烦\"}]";

        // 从 JSON 数组字符串中读取对象数组
        User[] users1 = mapper.readValue(jsonArray, User[].class);
        System.out.println(Arrays.toString(users1));

        // 从 JSON 数组字符串中读取对象列表
        List<User> userList = mapper.readValue(jsonArray, new TypeReference<List<User>>() {
        });
        userList.forEach(System.out::println);

        // 从 JSON 数组字符串中读取对象列表
        Map<String, Object> map = mapper.readValue(userJson,
                new TypeReference<Map<String, Object>>() {
                });
        map.entrySet().parallelStream().forEach(entry ->
                System.out.println(entry.getKey() + " ---> " + entry.getValue())
        );
    }

    @Test
    @SneakyThrows
    public void testParseJSONIntoJsonNode() {
        String userJson = "{\"name\":\"默烦\",\"age\":20,\"height\":177.5}";
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode1 = mapper.readValue(userJson, JsonNode.class);
        System.out.println(jsonNode1.get("name").asText());

        JsonNode jsonNode2 = mapper.readTree(userJson);
        System.out.println(jsonNode2.get("age").asInt());

        ObjectNode objectNode = ((ObjectNode) mapper.readTree(userJson));
        objectNode.with("other").put("type", "Student");
        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        System.out.println(string);
    }

    @Test
    @SneakyThrows
    public void testJsonNodeOperation() {
        String userJson =
                "{ \"name\" : \"mofan\", \"age\" : 20," +
                        "\"hobby\" : [\"music\", \"game\", \"study\"]," +
                        "\"other\" : { \"type\" : \"Student\" } }";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(userJson);

        // 获取 name 的字段值
        String name = jsonNode.get("name").asText();
        System.out.println(name);

        // 获取 age 的字段值
        System.out.println(jsonNode.get("age").asInt());

        // 获取 hobby 数组中第一个值
        String firstHobby = jsonNode.get("hobby").get(0).asText();
        System.out.println(firstHobby);

        // 获取嵌套的 JSON 串中 type 的值
        String type = jsonNode.get("other").get("type").asText();
        System.out.println(type);
    }

    @Test
    @SneakyThrows
    public void testAddDataToJSON() {
        String userJson = "{\"name\":\"默烦\",\"age\":20,\"height\":177.5}";
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode objectNode = mapper.readValue(userJson, ObjectNode.class);
        // 添加普通字段
        objectNode.put("weight", 65);
        // 再嵌套一个 JSON
        objectNode.with("other").put("type", "Student");
        // 添加一个数组
        objectNode.withArray("hobby").add("music").add("game").add("study");
        // 向数组指定位置添加一个值
        objectNode.withArray("hobby").insert(1, "write");
        // 向数组中嵌套一段 JSON
        objectNode.withArray("jsonArray").add(mapper.readValue(userJson, ObjectNode.class));
        // 向数组中添加一个 Object 节点
        objectNode.withArray("objectArray").addObject().putObject("Test").put("test", "test");

        System.out.println(mapper.writeValueAsString(objectNode));

        System.out.println("========================================");
        System.out.println("Test Node is Object? " + objectNode.get("objectArray").get(0).isObject());
        System.out.println(objectNode.get("weight"));
        System.out.println("other field is Object? " + objectNode.get("other").isObject());
        System.out.println(objectNode.get("other").get("type"));
        System.out.println(objectNode.get("hobby").get(0));
        System.out.println("========================================");

        // 删除普通字段
        objectNode.without("age");
        // 删除嵌套 JSON 中的字段
        objectNode.with("other").without("type");
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

        System.out.println(mapper.writeValueAsString(objectNode));
    }

    @Test
    @SneakyThrows
    public void testTransform() {
        String userJson = "{\"name\":\"默烦\",\"age\":20,\"height\":177.5}";
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // JsonNode 转 Object
        User user = mapper.readValue(userJson, User.class);
        System.out.println(user);
        // 或者
        JsonNode jsonNode1 = mapper.readTree(userJson);
        User user1 = mapper.treeToValue(jsonNode1, User.class);
        System.out.println(user1);

        // Object 转 JsonNode
        User user2 = new User("mofan", 18, 178.1);
        JsonNode jsonNode2 = mapper.valueToTree(user2);
        System.out.println(mapper.writeValueAsString(jsonNode2));
    }

    @Test
    @SneakyThrows
    public void testSerialization_1() {
        User user = new User("默烦", 19, 178.2);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // 写为字符串
        String str = mapper.writeValueAsString(user);
        System.out.println(str);

        // 写为文件
        File file = new File("test.json");
        mapper.writeValue(file, user);
        System.out.println(file.exists());

        // 写为字节流并读取
        byte[] bytes = mapper.writeValueAsBytes(user);
        System.out.println(mapper.readValue(bytes, User.class));
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
        System.out.println(str);
    }

    @Test
    @SneakyThrows
    public void testStreamingParser() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("simpleJSON.json");
        JsonGenerator generator = factory.createGenerator(file, JsonEncoding.UTF8);

        generator.writeStartObject();
        generator.writeStringField("msg", "Hello Jackson");
        generator.writeEndObject();
        generator.close();

        JsonParser parser = factory.createParser(file);
        JsonToken jsonToken = parser.nextToken();

        jsonToken = parser.nextToken();
        if ((jsonToken != JsonToken.FIELD_NAME) || !"msg".equals(parser.currentName())) {
            throw new RuntimeException("出现异常1");
        }

        jsonToken = parser.nextToken();
        if (jsonToken != JsonToken.VALUE_STRING) {
            throw new RuntimeException("出现异常2");
        }

        String msg = parser.getText();
        System.out.println("msg is : " + msg);
        parser.close();
    }

    @Test
    @SneakyThrows
    public void testJsonGenerator() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("testJsonGenerator.json");
        JsonGenerator generator = factory.createGenerator(file, JsonEncoding.UTF8);

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
        generator.close();

        // 数据写入结束，来读取一下
        Map<String, Object> map = mapper.readValue(file,
                new TypeReference<Map<String, Object>>() {
                });
        map.entrySet().parallelStream().forEach(entry ->
                System.out.println(entry.getKey() + " ---> " + entry.getValue())
        );

    }

    @Test
    @Disabled
    @SneakyThrows
    public void testGetToken() {
        JsonFactory jsonFactory = new JsonFactory();
        // 先执行 testJsonGenerator() 测试方法，确保存在 testJsonGenerator.json 文件
        File file = new File("testJsonGenerator.json");
        JsonParser parser = jsonFactory.createParser(file);

        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            System.out.println(jsonToken);
        }
    }

    @Test
    @SneakyThrows
    public void testJsonParser() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        File file = new File("testJsonGenerator.json");
        JsonParser parser = factory.createParser(file);
        while (!parser.isClosed()) {
            if (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
                String fieldName = parser.getCurrentName();
                System.out.println();

                // 移动到下一标记处
                parser.nextToken();
                if ("name".equals(fieldName)) {
                    System.out.println("name : " + parser.getValueAsString());
                }
                if ("age".equals(fieldName)) {
                    System.out.println("age : " + parser.getValueAsInt());
                }
                if ("isGirl".equals(fieldName)) {
                    System.out.println("isGirl : " + parser.getValueAsBoolean());
                }
                if ("hobby".equals(fieldName)) {
                    System.out.println("hobby : ");
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        System.out.println("    " + parser.getValueAsString());
                    }
                }
            }
        }
    }

    @Test
    @Disabled
    @SneakyThrows
    public void testAnnotation() {
        ObjectMapper mapper = new ObjectMapper();
        User user = new User("mofan", 19, 178.2);
        // 序列化
        String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        System.out.println(str);
        // 反序列化
        User readValue = mapper.readValue(str, User.class);
        System.out.println(readValue);
        String userJson = "{\"username\":\"默烦\",\"age\":20,\"height\":177.5}";
        User value = mapper.readValue(userJson, User.class);
        System.out.println(value);
    }

    @Test
    @SneakyThrows
    public void testSerializationAnnotation() {
        // 使用 @JsonRootName 注解后，必须启用 SerializationFeature.WRAP_ROOT_VALUE
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
        User user = new User("mofan", 19, 178.2);
        // 序列化
        String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        System.out.println(str);
    }

    @Test
    @SneakyThrows
    public void testJsonCreator() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\n" +
                "  \"stuName\": \"默烦\",\n" +
                "  \"age\": 20,\n" +
                "  \"height\": 177.5,\n" +
                "  \"school\": {\n" +
                "    \"name\": \"Test\"\n" +
                "  }\n" +
                "}";
        Student student = mapper.readValue(json, Student.class);
        System.out.println(student);
    }

    @Getter
    @ToString
    @FieldNameConstants
    static class Student {
        private final String stuName;
        private final int age;
        private final double height;
        private final School school;

        @JsonCreator
        public Student(@JsonProperty(Fields.stuName) String stuName,
                       @JsonProperty(Fields.age) int age,
                       @JsonProperty(Fields.height) double height,
                       @JsonProperty(Fields.school) School school) {
            this.stuName = stuName;
            this.age = age;
            this.height = height;
            this.school = school;
        }

        @Getter
        @ToString
        @FieldNameConstants
        static class School {
            private final String name;

            @JsonCreator
            public School(@JsonProperty(Fields.name) String name) {
                this.name = name;
            }
        }
    }

    @Test
    @SneakyThrows
    public void testConstructorProperties() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\n" +
                "  \"name\": \"mofan\",\n" +
                "  \"personAge\": 20\n" +
                "}";
        Person person = mapper.readValue(json, Person.class);
        System.out.println(person);
    }

    @ToString
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
        list.forEach(System.out::println);
        int[] ints = mapper.convertValue(list, int[].class);
        System.out.println(Arrays.toString(ints));
        // POJO to Map
        User user = new User("mofan", 19, 178.3);
        Map<String, Object> map = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {
        });
        map.entrySet().parallelStream().forEach(entry ->
                System.out.println(entry.getKey() + " ---> " + entry.getValue())
        );
        // Map to POJO
        User pojo = mapper.convertValue(map, User.class);
        System.out.println(pojo);
        // decode Base64
        String base64 = "bW9mYW4=";
        byte[] bytes = mapper.convertValue(base64, byte[].class);
        String decode = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("Base64 解码得 : " + decode);

        // POJO 转 JsonNode
        User people = new User("默烦", 19, 177.5);
        JsonNode jsonNode = mapper.convertValue(people, JsonNode.class);
        System.out.println(mapper.writeValueAsString(jsonNode));
    }

    @Test
    @SneakyThrows
    public void testLongValue() {
        String withLong = "{\n" +
                "  " +
                "\"id1\": \"98765432112345\",\n" +
                "  \"id2\": \"98765432112345L\",\n" +
                "  \"id3\": 98765432112345,\n" +
                "  \"id4\": \"1001\",\n" +
                "  \"id5\": \"1001L\",\n" +
                "  \"id6\": 1001\n" +
                "}\n";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = ((ObjectNode) mapper.readTree(withLong));
        JsonNode id1 = objectNode.get("id1");
        JsonNode id2 = objectNode.get("id2");
        JsonNode id3 = objectNode.get("id3");
        JsonNode id4 = objectNode.get("id4");
        JsonNode id5 = objectNode.get("id5");
        JsonNode id6 = objectNode.get("id6");
        System.out.println(id1.isLong());
        System.out.println(id2.isLong());
        System.out.println(id3.isLong());
        System.out.println(id4.isLong());
        System.out.println(id5.isLong());
        System.out.println(id6.isLong());
    }
}
