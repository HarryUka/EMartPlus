package com.emartplus.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import com.emartplus.EmartplusApplication;
import com.emartplus.dto.AuthResponse;
import com.emartplus.dto.LoginRequest;
import com.emartplus.dto.UserDto;
import com.emartplus.entity.Role;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.repository.RefreshTokenRepository;
import com.emartplus.security.JwtService;

@SpringBootTest(classes = EmartplusApplication.class)
@ActiveProfiles("test")
class AuthenticationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private Authentication authentication;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
            userService, 
            jwtService, 
            authenticationManager,
            refreshTokenService,
            refreshTokenRepository
        );
    }

    @Test
    void register_ValidUserDto_ReturnsAuthResponse() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");
        userDto.setFullName("Test User");

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword("encoded_password");
        user.setFullName(userDto.getFullName());
        user.setRole(Role.USER);

        when(userService.createUser(any(UserDto.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("test_token");

        // Act
        AuthResponse response = authenticationService.register(userDto);

        // Assert
        assertNotNull(response);
        assertEquals("test_token", response.getToken());
        assertEquals(userDto.getEmail(), response.getEmail());
        assertEquals(userDto.getFullName(), response.getFullName());
    }

    @Test
    void login_ValidCredentials_ReturnsAuthResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setFullName("Test User");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userService.getUserByEmail(loginRequest.getEmail())).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("test_token");

        // Act
        AuthResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_token", response.getToken());
        assertEquals(loginRequest.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
    }

    @Test
    void login_InvalidCredentials_ThrowsApiException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        assertThrows(ApiException.class, () -> authenticationService.login(loginRequest));
    }
} 