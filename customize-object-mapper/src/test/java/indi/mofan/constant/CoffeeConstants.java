package indi.mofan.constant;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;

/**
 * @author mofan
 * @date 2024/2/6 17:27
 */
public class CoffeeConstants {
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
}
