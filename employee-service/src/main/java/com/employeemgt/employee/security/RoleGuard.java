package com.employeemgt.employee.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleGuard implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RoleGuard.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        logger.info("RoleGuard.preHandle called for: {} {}", request.getMethod(), request.getRequestURI());
        
        if (!(handler instanceof HandlerMethod)) {
            logger.info("Handler is not HandlerMethod, allowing");
            return true; // Not a controller method, allow
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Check for @RoleRequired annotation on method or class
        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            roleRequired = handlerMethod.getBeanType().getAnnotation(RoleRequired.class);
        }
        
        if (roleRequired == null) {
            logger.info("No @RoleRequired annotation found, allowing");
            return true; // No role requirement, allow
        }

        // Get user role from header (set by API Gateway)
        String userRole = request.getHeader("X-User-Role");
        String userName = request.getHeader("X-User-Name");
        
        logger.info("Role guard - User: {}, Role: {}, Required: {}", 
                     userName, userRole, Arrays.toString(roleRequired.value()));

        if (userRole == null) {
            logger.warn("No user role found in headers for {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"User role not found\"}");
            response.getWriter().flush();
            return false;
        }

        // Check if user has required role
        boolean hasRequiredRole = Arrays.asList(roleRequired.value()).contains(userRole);
        
        if (!hasRequiredRole) {
            logger.warn("User {} with role {} attempted to access {} requiring roles {}", 
                        userName, userRole, request.getRequestURI(), Arrays.toString(roleRequired.value()));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"Insufficient privileges. Required roles: " + Arrays.toString(roleRequired.value()) + ", but user has role: " + userRole + "\"}");
            response.getWriter().flush();
            return false;
        }

        logger.info("Access granted for user {} with role {}", userName, userRole);
        return true;
    }
}