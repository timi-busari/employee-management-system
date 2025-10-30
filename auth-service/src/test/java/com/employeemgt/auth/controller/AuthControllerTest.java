package com.employeemgt.auth.controller;

import com.employeemgt.auth.config.SecurityConfig;
import com.employeemgt.auth.dto.AuthResponse;
import com.employeemgt.auth.dto.LoginRequest;
import com.employeemgt.auth.dto.RegisterRequest;
import com.employeemgt.auth.entity.Role;
import com.employeemgt.auth.exception.AuthenticationException;
import com.employeemgt.auth.exception.UserAlreadyExistsException;
import com.employeemgt.auth.service.AuthService;
import com.employeemgt.auth.service.UserService;
import com.employeemgt.auth.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean 
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setRole(Role.USER);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        authResponse = new AuthResponse("jwt-token", "testuser", "test@example.com");
    }

    @Test
    void register_WithValidData_ShouldReturnCreatedAndAuthResponse() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.type").value("Bearer"));
    }

    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkAndAuthResponse() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.type").value("Bearer"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProfile_WithAuthenticatedUser_ShouldReturnProfile() throws Exception {
        // Arrange
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("id", 1L);
        userProfile.put("username", "testuser");
        userProfile.put("email", "test@example.com");
        userProfile.put("firstName", "Test");
        userProfile.put("lastName", "User");
        userProfile.put("role", Role.USER);
        userProfile.put("createdAt", LocalDateTime.now());

        when(authService.getUserProfile("testuser")).thenReturn(userProfile);

        // Act & Assert
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Test"))
                .andExpect(jsonPath("$.data.lastName").value("User"));
    }

    @Test
    void getProfile_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Auth service is healthy"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("auth-service"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    void publicTest_ShouldReturnPublicEndpointResponse() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/public/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Public endpoint test successful"))
                .andExpect(jsonPath("$.data.access").value("No authentication required"))
                .andExpect(jsonPath("$.data.message").value("Public test endpoint working"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }
}