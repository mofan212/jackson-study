package indi.mofan.advanced;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
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

import java.io.Serial;
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

    private List<Vehicle> buildVehicleList() {
        Car car = new Car("Mercedes-Benz", "S500", 5, 250.0);
        Truck truck = new Truck("Isuzu", "NQR", 7500.0);
        return new ArrayList<>(List.of(car, truck));
    }

    private Fleet buildFleet() {
        Fleet serializedFleet = new Fleet();
        serializedFleet.setVehicles(buildVehicleList());
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

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties({"model", "seatingCapacity"})
    private static abstract class AnotherCar extends Vehicle {

        private int seatingCapacity;
        @JsonIgnore
        private double topSpeed;

        protected AnotherCar(String make, String model, int seatingCapacity, double topSpeed) {
            super(make, model);
            this.seatingCapacity = seatingCapacity;
            this.topSpeed = topSpeed;
        }
    }

    @NoArgsConstructor
    private static class Sedan extends AnotherCar {
        public Sedan(String make, String model, int seatingCapacity, double topSpeed) {
            super(make, model, seatingCapacity, topSpeed);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Crossover extends AnotherCar {
        private double towingCapacity;

        public Crossover(String make, String model, int seatingCapacity,
                         double topSpeed, double towingCapacity) {
            super(make, model, seatingCapacity, topSpeed);
            this.towingCapacity = towingCapacity;
        }
    }

    @Test
    @SneakyThrows
    public void testIgnoreSupertypePropertiesByAnnotations() {
        JsonMapper mapper = JsonMapper.builder().build();

        Sedan sedan = new Sedan("Mercedes-Benz", "S500", 5, 250.0);
        Crossover crossover = new Crossover("BMW", "X6", 5, 250.0, 6000.0);

        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(sedan);
        vehicles.add(crossover);

        String result = mapper.writeValueAsString(vehicles);
        // language=JSON
        String expectJson = """
                [
                  {
                    "make": "Mercedes-Benz"
                  },
                  {
                    "make": "BMW",
                    "towingCapacity": 6000.0
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static abstract class CarMixIn {
        /**
         * make 是在父类 Vehicle 中的
         */
        @JsonIgnore
        public String make;
        /**
         * topSpeed 是在当前类 Car 中的
         */
        @JsonIgnore
        public String topSpeed;
    }

    @Test
    @SneakyThrows
    public void testIgnoreSupertypePropertiesByMixIn() {
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(Car.class, CarMixIn.class)
                .build();

        List<Vehicle> vehicles = buildVehicleList();

        String result = mapper.writeValueAsString(vehicles);
        // language=JSON
        String expectJson = """
                [
                  {
                    "model": "S500",
                    "seatingCapacity": 5
                  },
                  {
                    "make": "Isuzu",
                    "model": "NQR",
                    "payloadCapacity": 7500.0
                  }
                ]
                """;
        /*
         * Car 上的 make 和 topSpeed 被忽略了，但 Truck 里的 make 没有被忽略
         */
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class IgnoranceIntrospector extends JacksonAnnotationIntrospector {
        @Serial
        private static final long serialVersionUID = 3450201475667905567L;

        public boolean hasIgnoreMarker(AnnotatedMember m) {
            /*
             * 忽略 Vehicle 中的 model
             * 忽略 Car 中的所有字段
             * 忽略名为 payloadCapacity 的字段
             */
            return m.getDeclaringClass() == Vehicle.class && "model".equals(m.getName())
                   || m.getDeclaringClass() == Car.class
                   || "payloadCapacity".equals(m.getName())
                   || super.hasIgnoreMarker(m);
        }
    }

    @Test
    @SneakyThrows
    public void testIgnoreSupertypePropertiesByAnnotationIntrospection() {
        JsonMapper mapper = JsonMapper.builder()
                .annotationIntrospector(new IgnoranceIntrospector())
                .build();

        List<Vehicle> vehicles = buildVehicleList();
        String result = mapper.writeValueAsString(vehicles);
        // language=JSON
        String expectJson = """
                [
                  {
                    "make": "Mercedes-Benz"
                  },
                  {
                    "make": "Isuzu"
                  }
                ]
                """;
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }

    private static class OtherCarMixIn extends Vehicle {
        @JsonIgnore
        private int seatingCapacity;

        @JsonIgnore
        private double topSpeed;
    }

    private static class OtherTruckMixIn extends Vehicle {
        @JsonIgnore
        private double payloadCapacity;
    }

    @Test
    public void testConversionBetweenSubtypes() {
        JsonMapper mapper = JsonMapper.builder()
                .addMixIn(Car.class, OtherCarMixIn.class)
                .addMixIn(Truck.class, OtherTruckMixIn.class)
                .build();

        Car car = new Car("Mercedes-Benz", "S500", 5, 250.0);
        Truck truck = mapper.convertValue(car, Truck.class);
        // Car -> Truck
        assertThat(truck).extracting(Vehicle::getMake, Vehicle::getModel)
                .containsExactly("Mercedes-Benz", "S500");
    }

    @Getter
    @AllArgsConstructor
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NoArgsCar.class, name = "car"),
            @JsonSubTypes.Type(value = NoArgsTruck.class, name = "truck")
    })
    private static abstract class NoArgsVehicle {
        private final String make;
        private final String model;
    }

    @Getter
    private static class NoArgsCar extends NoArgsVehicle {
        private final int seatingCapacity;
        private final double topSpeed;

        @JsonCreator
        public NoArgsCar(
                @JsonProperty("make") String make,
                @JsonProperty("model") String model,
                @JsonProperty("seating") int seatingCapacity,
                @JsonProperty("topSpeed") double topSpeed) {
            super(make, model);
            this.seatingCapacity = seatingCapacity;
            this.topSpeed = topSpeed;
        }
    }

    @Getter
    private static class NoArgsTruck extends NoArgsVehicle {
        private final double payloadCapacity;

        @JsonCreator
        public NoArgsTruck(
                @JsonProperty("make") String make,
                @JsonProperty("model") String model,
                @JsonProperty("payload") double payloadCapacity) {
            super(make, model);
            this.payloadCapacity = payloadCapacity;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class AnotherFleet {
        private List<NoArgsVehicle> vehicles;
    }

    @Test
    @SneakyThrows
    public void testDeserializationWithoutNoArgConstructors() {
        JsonMapper mapper = JsonMapper.builder().build();

        NoArgsCar car = new NoArgsCar("Mercedes-Benz", "S500", 5, 250.0);
        NoArgsTruck truck = new NoArgsTruck("Isuzu", "NQR", 7500.0);

        List<NoArgsVehicle> vehicles = new ArrayList<>();
        vehicles.add(car);
        vehicles.add(truck);

        JavaType type = mapper.getTypeFactory()
                .constructCollectionType(List.class, NoArgsVehicle.class);

        String result = mapper
                /*
                 * 需要指定序列化列表的元素类型，否则无法正确输出 type，
                 * 或者另外使用一个对象包装下 `List<NoArgsVehicle>` 也能达到要求
                 */
                .writerFor(type)
                .writeValueAsString(vehicles);
        // language=JSON
        String expectJson = """
                [
                  {
                    "type": "car",
                    "make": "Mercedes-Benz",
                    "model": "S500",
                    "topSpeed": 250.0,
                    "seatingCapacity": 5
                  },
                  {
                    "type": "truck",
                    "make": "Isuzu",
                    "model": "NQR",
                    "payloadCapacity": 7500.0
                  }
                ]""";
        JsonAssertions.assertThatJson(result).isEqualTo(expectJson);
    }
}
