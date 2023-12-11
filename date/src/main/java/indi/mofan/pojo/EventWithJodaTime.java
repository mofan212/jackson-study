package indi.mofan.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.mofan.serializer.CustomJodaDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mofan
 * @date 2023/12/11 15:32
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventWithJodaTime {

    public String name;

    @JsonSerialize(using = CustomJodaDateTimeSerializer.class)
    public Date eventDate;
}
