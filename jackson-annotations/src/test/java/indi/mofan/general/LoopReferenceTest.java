package indi.mofan.general;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
 * {@code @JsonManagedReference} 和 {@code @JsonBackReference} 用于处理在循环引用中的父子关系
 * </p>
 *
 * @author mofan
 * @date 2023/12/3 15:28
 * @link <a href="https://www.baeldung.com/jackson-annotations#5-jsonmanagedreference-jsonbackreference">@JsonManagedReference, @JsonBackReference</a>
 */
public class LoopReferenceTest implements WithAssertions {

    @AllArgsConstructor
    private static class ItemWithRef {
        public int id;
        public String itemName;

        @JsonManagedReference
        public UserWithRef owner;
    }

    @RequiredArgsConstructor
    private static class UserWithRef {
        public final int id;
        public final String name;

        @JsonBackReference
        public final List<ItemWithRef> userItems = new ArrayList<>();

        public void addItem(ItemWithRef item) {
            this.userItems.add(item);
        }
    }

    @Test
    @SneakyThrows
    public void testLoopReference() {
        UserWithRef user = new UserWithRef(1, "John");
        ItemWithRef item = new ItemWithRef(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder().build();
        String result = mapper.writeValueAsString(item);
        String expectJson = """
                {
                  "id": 2,
                  "itemName": "book",
                  "owner": {
                    "id": 1,
                    "name": "John"
                  }
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
