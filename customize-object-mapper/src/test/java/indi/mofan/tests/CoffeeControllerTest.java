package indi.mofan.tests;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author mofan
 * @date 2024/2/6 16:08
 */
@SpringBootTest
public class CoffeeControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @SneakyThrows
    public void testGetCoffee() {
        String expectJson = """
                {
                  "name": null,
                  "brand": "mofan",
                  "date": "2024-02-06T16:00:00"
                }
                """;
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/coffee?brand=mofan")
                .accept(MediaType.APPLICATION_JSON);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectJson));
    }
}
