package com.employeemgt.auth.service;

import com.employeemgt.auth.dto.AuthResponse;
import com.employeemgt.auth.dto.LoginRequest;
import com.employeemgt.auth.dto.RegisterRequest;
import com.employeemgt.auth.entity.Role;
import com.employeemgt.auth.entity.User;
import com.employeemgt.auth.exception.AuthenticationException;
import com.employeemgt.auth.exception.UserAlreadyExistsException;
import com.employeemgt.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

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

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void register_WithValidData_ShouldCreateUserAndReturnAuthResponse() {
        // Arrange
        when(userService.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(user);
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Bearer", response.getType());

        verify(userService).existsByUsername(registerRequest.getUsername());
        verify(userService).existsByEmail(registerRequest.getEmail());
        verify(userService).createUser(any(User.class));
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void register_WithExistingUsername_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userService.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userService).existsByUsername(registerRequest.getUsername());
        verify(userService, never()).existsByEmail(any());
        verify(userService, never()).createUser(any());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userService.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userService).existsByUsername(registerRequest.getUsername());
        verify(userService).existsByEmail(registerRequest.getEmail());
        verify(userService, never()).createUser(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Bearer", response.getType());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByUsername(loginRequest.getUsername());
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByUsername(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_WithUserNotFound_ShouldThrowAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userService.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails); // Token generation happens before user lookup
        verify(userService).findByUsername(loginRequest.getUsername());
    }

    @Test
    void getUserProfile_WithValidUsername_ShouldReturnUserProfile() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        Map<String, Object> profile = authService.getUserProfile("testuser");

        // Assert
        assertNotNull(profile);
        assertEquals(1L, profile.get("id"));
        assertEquals("testuser", profile.get("username"));
        assertEquals("test@example.com", profile.get("email"));
        assertEquals("Test", profile.get("firstName"));
        assertEquals("User", profile.get("lastName"));
        assertEquals(Role.USER, profile.get("role"));
        assertNotNull(profile.get("createdAt"));

        verify(userService).findByUsername("testuser");
    }

    @Test
    void getUserProfile_WithInvalidUsername_ShouldThrowAuthenticationException() {
        // Arrange
        when(userService.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.getUserProfile("invaliduser");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername("invaliduser");
    }
}