package indi.mofan;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/21 19:26
 * @link <a href="https://www.baeldung.com/jackson-bidirectional-relationships-and-infinite-recursion">Jackson – Bidirectional Relationships</a>
 */
public class BidirectionalRelationshipsTest implements WithAssertions {
    @Getter
    @Setter
    @NoArgsConstructor
    private static class User {
        private int id;
        private String name;
        private List<Item> userItems;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
            this.userItems = new ArrayList<>();
        }

        public void addItem(Item item) {
            if (this.userItems == null) {
                this.userItems = new ArrayList<>();
            }
            this.userItems.add(item);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Item {
        private int id;
        private String itemName;
        private User owner;
    }

    @Test
    public void testInfiniteRecursion() {
        User user = new User(1, "John");
        Item item = new Item(2, "book", user);
        user.addItem(item);

        JsonMapper mapper = JsonMapper.builder().build();
        assertThatExceptionOfType(JsonMappingException.class)
                .isThrownBy(() -> mapper.writeValueAsString(item));
    }
}
