package indi.mofan.serializer.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author mofan
 * @date 2023/12/3 22:27
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public Integer id;
    public String name;
}
