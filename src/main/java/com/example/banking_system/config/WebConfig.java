package com.example.banking_system.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig provides custom web MVC configurations, including resource handlers.
 * This is sometimes needed if auto-configuration for static resources (like Swagger UI)
 * is not working as expected.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Adds resource handlers to serve static content from webjars.
     * This explicitly maps requests for "/webjars/**" to the classpath location
     * where webjar resources (like Swagger UI's files) are found.
     *
     * @param registry The ResourceHandlerRegistry to add handlers to.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
