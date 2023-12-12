package indi.mofan.annotation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

/**
 * @author mofan
 * @date 2023/7/18 10:23
 * @see com.fasterxml.jackson.annotation.JsonBackReference
 */
public class JsonBackReferenceTest implements WithAssertions {
    @Getter
    @Setter
    static class Employer {
        private String name;
        @JsonManagedReference
        private List<Employee> employees;
    }

    @Getter
    @Setter
    static class Employee {
        private String name;
        @JsonBackReference
        private Employer employer;
    }

    @Test
    @SneakyThrows
    public void test() {
        Employee employee = new Employee();
        employee.setName("mofan");

        Employer employer = new Employer();
        employer.setName("默烦");
        employer.setEmployees(List.of(employee));

        employee.setEmployer(employer);

        JsonMapper mapper = JsonMapper.builder().build();
        String json = mapper.writeValueAsString(employer);
        String expectJson = """
                {
                  "name": "默烦",
                  "employees": [
                    {
                      "name": "mofan"
                    }
                  ]
                }
                """;
        // 效果与使用了 @JsonIgnore 类似，没有序列化被 @JsonBackReference 标记的字段
        JsonAssertions.assertThatJson(json).isEqualTo(expectJson);

        // 反序列化呢？在 Employer 中的 employees 上使用 @JsonManagedReference 注解
        Employer value = mapper.readValue(expectJson, Employer.class);
        assertThat(value.getEmployees()).map(Employee::getEmployer)
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .map(Employer::getName)
                .singleElement()
                .isEqualTo("默烦");
    }
}
