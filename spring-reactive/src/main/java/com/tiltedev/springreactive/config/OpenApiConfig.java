package com.tiltedev.springreactive.config;

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
                        .title("Backend demo of various Spring features")
                        .description(
                                "This isn't a real product - it's a showcase of how I architect a full-stack app, pairing this Vue 3 + TypeScript client with a reactive Spring WebFlux backend. Each page below demonstrates a different technique.")
                        .version("0.0.1"));
    }
}
