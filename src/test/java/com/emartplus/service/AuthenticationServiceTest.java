package com.emartplus.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.emartplus.dto.AuthResponse;
import com.emartplus.dto.LoginRequest;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.security.JwtService;

@SpringBootTest
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void shouldLoginWithValidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setFullName("Test User");

        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(userService.getUserByEmail(loginRequest.getEmail())).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("test_token");

        // Act
        AuthResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_token", response.getToken());
        assertEquals(loginRequest.getEmail(), response.getEmail());
    }

    @Test
    void shouldThrowExceptionWithInvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrong_password");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        assertThrows(ApiException.class, () -> authenticationService.login(loginRequest));
    }
} 