package com.example.expenses.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the backend API.
 *
 * Reads the comma separated allowed origins from the property
 * 'app.cors.allowed-origins'. In environments like Docker/Render you can
 * set the environment variable APP_CORS_ALLOWED_ORIGINS to configure this.
 * If the property/variable is empty the known deployed frontend origin is used
 * as a safe default (no wildcard * in production).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Default deployed frontend origin from the issue when no env var/property is provided.
    private static final String DEFAULT_ALLOWED_ORIGIN = "https://expense-management-system-evszjcltk-jathin6.vercel.app";

    // Property: app.cors.allowed-origins (can be set via env var APP_CORS_ALLOWED_ORIGINS)
    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = parseAllowedOrigins(allowedOriginsProp);
        if (origins.isEmpty()) {
            origins = List.of(DEFAULT_ALLOWED_ORIGIN);
        }

        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type");
                // Intentionally not allowing wildcard '*' for production; origins are explicit
    }

    private List<String> parseAllowedOrigins(String prop) {
        if (prop == null || prop.isBlank()) {
            return List.of();
        }
        return Arrays.stream(prop.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
