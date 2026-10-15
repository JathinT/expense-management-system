package com.example.expenses.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Custom CORS handling for the backend API.
 *
 * Instead of relying on the MVC CORS registration (which can cause the framework
 * to reject disallowed origins with a 403 before controller handling), we
 * register a small servlet Filter. The filter will:
 * - Add Access-Control-Allow-* headers only when the request Origin is in the
 *   configured allow-list.
 * - Short-circuit OPTIONS preflight for allowed origins with the appropriate
 *   headers.
 * - For disallowed origins: do not add CORS headers but allow the request to
 *   proceed (so the server returns its normal response and the browser will
 *   block cross-origin access as expected).
 */
@Configuration
public class CorsConfig {

    // Default deployed frontend origin (current production Vercel URL).
    private static final String DEFAULT_ALLOWED_ORIGIN = "https://expense-management-system-phi.vercel.app";

    // Property: app.cors.allowed-origins (can be set via env var APP_CORS_ALLOWED_ORIGINS)
    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Bean
    public FilterRegistrationBean<Filter> corsFilterRegistration() {
        Filter corsFilter = new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;

                List<String> allowed = parseAllowedOrigins(allowedOriginsProp);
                if (allowed.isEmpty()) {
                    allowed = List.of(DEFAULT_ALLOWED_ORIGIN);
                }

                String origin = req.getHeader("Origin");
                boolean originAllowed = origin != null && allowed.stream().anyMatch(o -> o.equalsIgnoreCase(origin));

                if (originAllowed) {
                    // Set CORS response headers for allowed origins. We set them before
                    // the chain so they will be present even if later processing
                    // results in an error response.
                    res.setHeader("Access-Control-Allow-Origin", origin);
                    res.setHeader("Vary", "Origin");
                    res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                    res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
                    // Do not force credentials value here; keep default behavior unless needed.
                }

                // Handle preflight: if allowed origin, respond immediately with 200 and headers.
                if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                    // For preflight from an allowed origin we already set headers above.
                    // Return OK in all cases (no body). If origin not allowed, return 200
                    // with no CORS headers so the browser's preflight will fail client-side.
                    res.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                // For non-OPTIONS, continue filter chain. If origin was disallowed the
                // request is allowed to proceed and will not have CORS headers added.
                chain.doFilter(request, response);
            }
        };

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(corsFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
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
