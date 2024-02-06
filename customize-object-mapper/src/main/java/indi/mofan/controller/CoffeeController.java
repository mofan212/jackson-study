package indi.mofan.controller;

import indi.mofan.pojo.Coffee;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author mofan
 * @date 2024/2/6 16:04
 */
@RestController
public class CoffeeController {
    @GetMapping("/coffee")
    public Coffee getCoffee(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String name) {
        Coffee coffee = new Coffee();
        coffee.setBrand(brand);
        coffee.setName(name);
        coffee.setDate(LocalDateTime.of(2024, 2, 6, 16, 0));
        return coffee;
    }
}
