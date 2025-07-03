package com.example.banking_system.config;



import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig class for configuring Springdoc OpenAPI (Swagger UI).
 * This class provides metadata for the generated API documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the OpenAPI bean, which provides global information about the API.
     *
     * @return An OpenAPI instance.
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Banking System API")
                        .description("API documentation for the Banking System application, covering Utility Bill Payments, Banking Operations, and Loan Management.")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Banking System Wiki Documentation")
                        .url("https://example.com/docs")); // Replace with your actual documentation URL
    }
}