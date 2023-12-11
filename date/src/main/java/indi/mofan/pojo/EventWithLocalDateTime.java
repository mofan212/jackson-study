package indi.mofan.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.mofan.serializer.CustomJava8LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author mofan
 * @date 2023/12/11 22:44
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventWithLocalDateTime {
    public String name;

    @JsonSerialize(using = CustomJava8LocalDateTimeSerializer.class)
    public LocalDateTime eventDate;
}
