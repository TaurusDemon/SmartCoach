package com.smartcoach.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI().
                info(new Info().title("SmartCoach AI API").
                        version("1.0.0").
                        description("Backend con Arquitectura Hexagonal para la plataforma Smart Coach AI.").
                        contact(new Contact().
                                name("Smart Coach Team").
                                email("contact@smartcoach.com")));
    }
}
