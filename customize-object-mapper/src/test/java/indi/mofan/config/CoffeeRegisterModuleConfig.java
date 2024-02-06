package indi.mofan.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import indi.mofan.constant.CoffeeConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * @author mofan
 * @date 2024/2/6 16:49
 */
@Configuration
@Profile("custom-properties")
@PropertySource("classpath:coffee.properties")
public class CoffeeRegisterModuleConfig {

    @Bean
    public Module javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(CoffeeConstants.LOCAL_DATETIME_SERIALIZER);
        return module;
    }
}
