package com.emartplus.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.emartplus.dto.AuthResponse;
import com.emartplus.dto.LoginRequest;
import com.emartplus.dto.TokenRefreshRequest;
import com.emartplus.dto.TokenRefreshResponse;
import com.emartplus.dto.UserDto;
import com.emartplus.entity.RefreshToken;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.repository.RefreshTokenRepository;
import com.emartplus.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

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

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new ApiException("Refresh token not found", HttpStatus.NOT_FOUND));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build()
        );

        return new TokenRefreshResponse(token, refreshToken.getToken(), "Bearer");
    }

    public void logout(String email) {
        User user = userService.getUserByEmail(email);
        refreshTokenService.deleteByUserId(user.getId());
    }
} 