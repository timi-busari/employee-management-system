package com.employeemgt.auth.service;

import com.employeemgt.auth.dto.AuthResponse;
import com.employeemgt.auth.dto.LoginRequest;
import com.employeemgt.auth.dto.RegisterRequest;
import com.employeemgt.auth.entity.User;
import com.employeemgt.auth.exception.AuthenticationException;
import com.employeemgt.auth.exception.UserAlreadyExistsException;
import com.employeemgt.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("User not found"));
            
            return new AuthResponse(token, user.getUsername(), user.getEmail());
            
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid username or password");
        }
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        
        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        
        // Create new user
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            registerRequest.getPassword(),
            registerRequest.getFirstName(),
            registerRequest.getLastName()
        );
        
        User savedUser = userService.createUser(user);
        
        // Generate token for the new user
        UserDetails userDetails = userService.loadUserByUsername(savedUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail());
    }

    public Map<String, Object> getUserProfile(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());
        
        return profile;
    }
}