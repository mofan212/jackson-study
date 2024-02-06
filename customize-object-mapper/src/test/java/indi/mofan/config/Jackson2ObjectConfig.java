package indi.mofan.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import indi.mofan.constant.CoffeeConstants;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author mofan
 * @date 2024/2/6 17:26
 */
@Configuration
@Profile("jackson-2-object-mapper-builder-customizer")
public class Jackson2ObjectConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .serializers(CoffeeConstants.LOCAL_DATETIME_SERIALIZER);
    }
}
