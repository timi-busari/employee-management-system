package com.employeemgt.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction() {
        return RouterFunctions
                // Main Swagger UI page
                .route(GET("/docs"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource("static/index.html")))
                
                // Alternative Swagger UI endpoints
                .andRoute(GET("/swagger"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource("static/index.html")))
                
                .andRoute(GET("/api-docs"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource("static/index.html")))
                
                // OpenAPI YAML specification
                .andRoute(GET("/docs/openapi.yml"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.valueOf("application/x-yaml"))
                                .bodyValue(new ClassPathResource("static/openapi.yml")))
                
                .andRoute(GET("/swagger/openapi.yml"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.valueOf("application/x-yaml"))
                                .bodyValue(new ClassPathResource("static/openapi.yml")));
    }
}
