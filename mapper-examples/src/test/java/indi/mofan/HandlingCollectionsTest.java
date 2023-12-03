package indi.mofan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import indi.mofan.pojo.Car;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author mofan
 * @date 2023/12/3 23:24
 * @link <a href="https://www.baeldung.com/jackson-object-mapper-tutorial#4-handling-collections">Handling Collections</a>
 */
public class HandlingCollectionsTest implements WithAssertions {
    // language=JSON
    private static final String JSON_CAR_ARRAY = """
            [
              {
                "color": "Black",
                "type": "BMW"
              },
              {
                "color": "Red",
                "type": "FIAT"
              }
            ]""";

    @Test
    @SneakyThrows
    public void testHandlingCollections() {
        // as an array
        JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                .build();
        Car[] cars = mapper.readValue(JSON_CAR_ARRAY, Car[].class);
        assertThat(cars).hasSize(2)
                .extracting(Car::getColor, Car::getType)
                .containsExactly(tuple("Black", "BMW"), tuple("Red", "FIAT"));

        // as a List
        mapper = JsonMapper.builder().build();
        List<Car> carList = mapper.readValue(JSON_CAR_ARRAY, new TypeReference<>() {
        });
        assertThat(carList).hasSize(2)
                .extracting(Car::getColor, Car::getType)
                .containsExactly(tuple("Black", "BMW"), tuple("Red", "FIAT"));
    }
}
