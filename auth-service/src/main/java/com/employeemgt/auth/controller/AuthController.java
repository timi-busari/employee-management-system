package com.employeemgt.auth.controller;

import com.employeemgt.auth.dto.ApiResponse;
import com.employeemgt.auth.dto.AuthResponse;
import com.employeemgt.auth.dto.LoginRequest;
import com.employeemgt.auth.dto.RegisterRequest;
import com.employeemgt.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> profile = authService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "auth-service");
        healthData.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(ApiResponse.success("Auth service is healthy", healthData));
    }

    @GetMapping("/public/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publicTest() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("access", "No authentication required");
        testData.put("message", "Public test endpoint working");
        testData.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("Public endpoint test successful", testData));
    }
}