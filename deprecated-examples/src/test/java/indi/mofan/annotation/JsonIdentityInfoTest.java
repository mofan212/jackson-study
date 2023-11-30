package indi.mofan.annotation;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author mofan
 * @date 2023/7/16 22:45
 * @link <a href="https://www.logicbig.com/tutorials/misc/jackson/json-identity-info-annotation.html">Jackson JSON - Using @JsonIdentityInfo to handle circular references</a>
 * @see com.fasterxml.jackson.annotation.JsonIdentityInfo
 */
public class JsonIdentityInfoTest {
    @Getter
    @Setter
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class Customer {
        private Long id;
        private String name;
        private Order order;
    }

    @Getter
    @Setter
    static class Order {
        private Long orderId;
        private List<Integer> itemIds;
        private Customer customer;
    }

    @Test
    @SneakyThrows
    public void testSimplyUse() {
        Order order = new Order();
        order.setOrderId(1L);
        order.setItemIds(List.of(10, 30));

        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Peter");
        // customer 依赖 order
        customer.setOrder(order);
        // order 又依赖 custom，出现依赖循环
        order.setCustomer(customer);

        JsonMapper mapper = JsonMapper.builder().build();
        // 序列化
        String json = mapper.writeValueAsString(customer);
        // language=json
        String expectJson = """
                {
                  "id": 2,
                  "name": "Peter",
                  "order": {
                    "orderId": 1,
                    "itemIds": [10, 30],
                    "customer": 2
                  }
                }
                 """;
        // order 虽然依赖了 customer，但在使用 @JsonIdentityInfo 注解后，以指定的 id 作为代替
        JsonAssertions.assertThatJson(json).isEqualTo(expectJson);

        Customer newCustom = mapper.readValue(json, Customer.class);
        Assertions.assertThat(newCustom).isEqualTo(newCustom.getOrder().getCustomer()).extracting(
                Customer::getId, Customer::getName,
                i -> i.getOrder().getOrderId(), i -> i.getOrder().getItemIds()
        ).containsSequence(2L, "Peter", 1L, List.of(10, 30));
        Assertions.assertThat(newCustom).extracting(i -> i.getOrder().getCustomer())
                .extracting(Customer::getId, Customer::getName)
                .containsSequence(2L, "Peter");
    }

    @Getter
    @Setter
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "empId")
    static class Employee {
        private Long empId;
        private List<Dept> depts;
    }

    @Getter
    @Setter
    static class Dept {
        private Long deptId;
        private List<Employee> employees;
    }

    @Test
    @SneakyThrows
    public void testOne2Many() {
        Employee employee = new Employee();
        employee.setEmpId(1L);

        Dept dept1 = new Dept();
        dept1.setDeptId(11L);
        dept1.setEmployees(Collections.singletonList(employee));

        Dept dept2 = new Dept();
        dept2.setDeptId(12L);
        dept2.setEmployees(Collections.singletonList(employee));

        employee.setDepts(List.of(dept1, dept2));

        JsonMapper mapper = JsonMapper.builder().build();
        String json = mapper.writeValueAsString(employee);
        // language=json
        String expectJson = """
                {
                  "empId": 1,
                  "depts": [
                    {
                      "deptId": 11,
                      "employees": [1]
                    },
                    {
                      "deptId": 12,
                      "employees": [1]
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(json).isEqualTo(expectJson);

        Employee newEmp = mapper.readValue(json, Employee.class);
        Assertions.assertThat(newEmp).extracting(
                i -> i.getDepts().get(0).getEmployees().get(0).getEmpId(),
                i -> i.getDepts().get(1).getEmployees().get(0).getEmpId()
        ).containsSequence(1L, 1L);
    }

    @Getter
    @Setter
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@key")
    static class Entity {
        private String name;
        private List<Entity> entities;
    }

    @Test
    @SneakyThrows
    public void testIntSequenceGenerator() {
        Entity entity = new Entity();
        entity.setName("Root");

        Entity firstSubEntity = new Entity();
        firstSubEntity.setName("SubEntity-1");
        Entity secondSubEntity = new Entity();
        secondSubEntity.setName("SubEntity-2");

        entity.setEntities(List.of(entity, firstSubEntity, secondSubEntity));

        JsonMapper mapper = JsonMapper.builder().build();
        String expectJson = """
                {
                  "@key": 1,
                  "name": "Root",
                  "entities": [
                    1,
                    {
                      "@key": 2,
                      "name": "SubEntity-1",
                      "entities": null
                    },
                    {
                      "@key": 3,
                      "name": "SubEntity-2",
                      "entities": null
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(mapper.writeValueAsString(entity))
                .isEqualTo(expectJson);
    }
}
