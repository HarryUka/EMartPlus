package com.emartplus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emartplus.dto.AuthResponse;
import com.emartplus.dto.LoginRequest;
import com.emartplus.dto.UserDto;
import com.emartplus.entity.User;
import com.emartplus.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto userDto) {
        User user = userService.createUser(userDto);
        // For now returning simple response, will add JWT token later
        return ResponseEntity.ok(new AuthResponse(
            "dummy-token", 
            user.getEmail(), 
            user.getFullName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());
        // Will add proper authentication later
        return ResponseEntity.ok(new AuthResponse(
            "dummy-token",
            user.getEmail(),
            user.getFullName()
        ));
    }
} 