package indi.mofan.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.mofan.serializer.CustomDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/11 15:15
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventWithSerializer {

    public String name;

    @JsonSerialize(using = CustomDateSerializer.class)
    public Date eventDate;
}
