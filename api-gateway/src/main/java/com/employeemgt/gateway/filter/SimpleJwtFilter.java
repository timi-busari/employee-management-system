package com.employeemgt.gateway.filter;

import com.employeemgt.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SimpleJwtFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleJwtFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        
        logger.debug("JWT Filter processing path: {}", path);
        
        // Skip auth for login/register and health endpoints
        if (isPublicPath(path)) {
            logger.debug("Public path detected, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }
        
        String token = extractToken(request);
        logger.debug("Extracted token: {}", token != null ? "Present" : "Missing");
        
        if (token == null) {
            logger.debug("No JWT token found in request");
            return unauthorized(exchange);
        }
        
        try {
            String username = jwtUtil.extractUsername(token);
            logger.debug("Extracted username from token: {}", username);
            
            if (username == null || jwtUtil.isTokenExpired(token)) {
                logger.debug("JWT token validation failed - username: {}, expired: {}", 
                           username, jwtUtil.isTokenExpired(token));
                return unauthorized(exchange);
            }
            
            logger.debug("JWT token validation successful for user: {}", username);
        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
            return unauthorized(exchange);
        }
        
        // Add user info to headers (simple)
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        
        logger.debug("Adding headers - Username: {}, Role: {}", username, role);
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Name", username)
            .header("X-User-Role", role != null ? role : "USER")
            .build();
            
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") || 
               path.contains("/health") || 
               path.contains("/actuator") ||
               path.equals("/") ||
               path.equals("/favicon.ico");
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Access token is missing or invalid\"}";
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -1; // High priority to run before other filters
    }
}