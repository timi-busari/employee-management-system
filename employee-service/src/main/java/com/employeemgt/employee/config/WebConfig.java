package com.employeemgt.employee.config;

import com.employeemgt.employee.security.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoleGuard roleGuard;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleGuard)
                .addPathPatterns("/api/employees/**", "/api/departments/**")
                .excludePathPatterns(
                    "/api/employees/health", 
                    "/api/departments/health",
                    "/api/employees/public/**",
                    // Swagger/OpenAPI endpoints
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                );
    }
}