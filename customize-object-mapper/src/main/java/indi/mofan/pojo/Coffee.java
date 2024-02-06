package indi.mofan.pojo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author mofan
 * @date 2024/2/6 16:00
 */
@Getter
@Setter
public class Coffee {
    private String name;
    private String brand;
    private LocalDateTime date;
}
