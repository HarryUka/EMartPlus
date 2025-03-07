package com.emartplus.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.emartplus.dto.AuthResponse;
import com.emartplus.dto.LoginRequest;
import com.emartplus.dto.UserDto;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(UserDto userDto) {
        User user = userService.createUser(userDto);
        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build()
        );
        
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            var user = userService.getUserByEmail(loginRequest.getEmail());
            var token = jwtService.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
            );

            return new AuthResponse(token, user.getEmail(), user.getFullName());
        } catch (Exception e) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
} 