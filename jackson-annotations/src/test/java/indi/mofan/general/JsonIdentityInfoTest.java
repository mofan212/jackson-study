package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * {@code @JsonIdentityInfo} 与 {@code @JsonManagedReference}、{@code @JsonBackReference}
 * 类似，都可以用来处理无限递归问题。{@code @JsonIdentityInfo} 用于指示序列化或反序列化时应使用的对象标识
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 15:43
 * @link <a href="https://www.baeldung.com/jackson-annotations#6-jsonidentityinfo">JsonIdentityInfo</a>
 */
public class JsonIdentityInfoTest implements WithAssertions {

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @AllArgsConstructor
    private static class ItemWithIdentity {
        public int id;
        public String itemName;
        public UserWithIdentity owner;
    }

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @RequiredArgsConstructor
    private static class UserWithIdentity {
        public final int id;
        public final String name;
        public List<ItemWithIdentity> userItems = new ArrayList<>();

        public void addItem(ItemWithIdentity item) {
            this.userItems.add(item);
        }
    }

    @Test
    @SneakyThrows
    public void testJsonIdentityInfo() {
        UserWithIdentity user = new UserWithIdentity(1, "John");
        ItemWithIdentity item = new ItemWithIdentity(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(item);
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John",
                    "userItems": [
                      2
                    ]
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
