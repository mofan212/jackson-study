package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serial;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mofan
 * @date 2024/2/2 17:17
 */
public class MappingNestedValuesTest {
    // language=JSON
    private static final String SOURCE_JSON = """
            {
              "id": "957c43f2-fa2e-42f9-bf75-6e3d5bb6960a",
              "name": "The Best Product",
              "brand": {
                "id": "9bcd817d-0141-42e6-8f04-e5aaab0980b6",
                "name": "ACME Products",
                "owner": {
                  "id": "b21a80b1-0c09-4be3-9ebd-ea3653511c13",
                  "name": "Ultimate Corp, Inc."
                }
              }
            }
            """;

    @Getter
    @Setter
    private static class Product {
        private String id;
        private String name;
        private String brandName;
        private String ownerName;

        @JsonProperty("brand")
        @SuppressWarnings("unchecked")
        private void unpackNested(Map<String, Object> brand) {
            this.brandName = MapUtils.getString(brand, "name");
            Map<String, String> owner = (Map<String, String>) MapUtils.getMap(brand, "owner", Collections.emptyMap());
            this.ownerName = MapUtils.getString(owner, "name");
        }
    }

    @Test
    @SneakyThrows
    public void testMappingWithAnnotations() {
        JsonMapper mapper = JsonMapper.builder().build();
        Product product = mapper.readValue(SOURCE_JSON, Product.class);
        assertThat(product).extracting(Product::getName, Product::getBrandName, Product::getOwnerName)
                .containsExactly("The Best Product", "ACME Products", "Ultimate Corp, Inc.");
    }

    @Getter
    @Setter
    private static class SimpleProduct {
        private String id;
        private String name;
        private String brandName;
        private String ownerName;
    }

    private static class ProductDeserializer extends StdDeserializer<SimpleProduct> {
        @Serial
        private static final long serialVersionUID = 8515461382158181261L;

        public ProductDeserializer() {
            this(null);
        }

        public ProductDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public SimpleProduct deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

            JsonNode productNode = jp.getCodec().readTree(jp);
            SimpleProduct product = new SimpleProduct();
            product.setId(productNode.get("id").textValue());
            product.setName(productNode.get("name").textValue());
            product.setBrandName(productNode.get("brand")
                    .get("name").textValue());
            product.setOwnerName(productNode.get("brand").get("owner")
                    .get("name").textValue());
            return product;
        }
    }

    @Test
    @SneakyThrows
    public void testManualRegisterDeserializer() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimpleProduct.class, new ProductDeserializer());
        JsonMapper mapper = JsonMapper.builder()
                .addModule(module)
                .build();

        Product product = mapper.readValue(SOURCE_JSON, Product.class);
        assertThat(product).extracting(Product::getName, Product::getBrandName, Product::getOwnerName)
                .containsExactly("The Best Product", "ACME Products", "Ultimate Corp, Inc.");
    }

    @JsonDeserialize(using = ProductDeserializer.class)
    private static class SimpleProductMixIn {
    }

    @Test
    @SneakyThrows
    public void testAutomaticRegisterDeserializer() {
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(SimpleProduct.class, SimpleProductMixIn.class)
                .build();

        Product product = mapper.readValue(SOURCE_JSON, Product.class);
        assertThat(product).extracting(Product::getName, Product::getBrandName, Product::getOwnerName)
                .containsExactly("The Best Product", "ACME Products", "Ultimate Corp, Inc.");
    }
}
