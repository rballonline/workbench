package com.tiltedev.spring_reactive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Places I'd Like to Visit API")
                        .description("Collaborative travel wishlist - search cities, add destinations, track them live.")
                        .version("0.0.1"));
    }
}
