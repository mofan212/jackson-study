package indi.mofan.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/11 11:24
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    public String name;
    public Date eventDate;
}
