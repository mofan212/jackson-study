package indi.mofan.serializer.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author mofan
 * @date 2023/12/3 22:28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    public Integer id;
    public String itemName;
    public User owner;
}
