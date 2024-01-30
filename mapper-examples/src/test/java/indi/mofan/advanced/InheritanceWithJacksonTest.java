package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mofan
 * @date 2024/1/30 22:31
 * @link <a href="https://www.baeldung.com/jackson-inheritance">Inheritance with Jackson</a>
 */
public class InheritanceWithJacksonTest implements WithAssertions {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static abstract class Vehicle {
        private String make;
        private String model;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Car extends Vehicle {
        private int seatingCapacity;
        private double topSpeed;

        public Car(String make, String model, int seatingCapacity, double topSpeed) {
            super(make, model);
            this.seatingCapacity = seatingCapacity;
            this.topSpeed = topSpeed;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Truck extends Vehicle {
        private double payloadCapacity;

        public Truck(String make, String model, double payloadCapacity) {
            super(make, model);
            this.payloadCapacity = payloadCapacity;
        }
    }

    @Getter
    @Setter
    private static class Fleet {
        private List<Vehicle> vehicles;
    }

    private Fleet buildFleet() {
        Car car = new Car("Mercedes-Benz", "S500", 5, 250.0);
        Truck truck = new Truck("Isuzu", "NQR", 7500.0);

        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(car);
        vehicles.add(truck);

        Fleet serializedFleet = new Fleet();
        serializedFleet.setVehicles(vehicles);
        return serializedFleet;
    }

    @Test
    @SneakyThrows
    public void testGlobalDefaultTyping() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("indi.mofan.advanced.InheritanceWithJacksonTest")
                .allowIfSubType("java.util.ArrayList")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        Fleet serializedFleet = buildFleet();
        String result = mapper.writeValueAsString(serializedFleet);
        // language=JSON
        String expectJson = """
                [
                  "indi.mofan.advanced.InheritanceWithJacksonTest$Fleet",
                  {
                    "vehicles": [
                      "java.util.ArrayList",
                      [
                        [
                          "indi.mofan.advanced.InheritanceWithJacksonTest$Car",
                          {
                            "make": "Mercedes-Benz",
                            "model": "S500",
                            "seatingCapacity": 5,
                            "topSpeed": 250.0
                          }
                        ],
                        [
                          "indi.mofan.advanced.InheritanceWithJacksonTest$Truck",
                          {
                            "make": "Isuzu",
                            "model": "NQR",
                            "payloadCapacity": 7500.0
                          }
                        ]
                      ]
                    ]
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Fleet fleet = mapper.readValue(result, Fleet.class);
        List<Vehicle> vehicles = fleet.getVehicles();
        assertThat(vehicles.getFirst()).isExactlyInstanceOf(Car.class);
        assertThat(vehicles.getLast()).isExactlyInstanceOf(Truck.class);
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Car.class, name = "car"),
            @JsonSubTypes.Type(value = Truck.class, name = "truck")
    })
    private static abstract class VehicleMixIn {
    }

    @Test
    @SneakyThrows
    public void testPerClassAnnotations() {
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(Vehicle.class, VehicleMixIn.class)
                .build();

        Fleet fleet = buildFleet();
        String result = mapper.writeValueAsString(fleet);
        // language=JSON
        String expectJson = """
                {
                  "vehicles": [
                    {
                      "type": "car",
                      "make": "Mercedes-Benz",
                      "model": "S500",
                      "seatingCapacity": 5,
                      "topSpeed": 250.0
                    },
                    {
                      "type": "truck",
                      "make": "Isuzu",
                      "model": "NQR",
                      "payloadCapacity": 7500.0
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);

        Fleet value = mapper.readValue(result, Fleet.class);
        List<Vehicle> vehicles = value.getVehicles();
        assertThat(vehicles.getFirst()).isExactlyInstanceOf(Car.class);
        assertThat(vehicles.getLast()).isExactlyInstanceOf(Truck.class);
    }
}
