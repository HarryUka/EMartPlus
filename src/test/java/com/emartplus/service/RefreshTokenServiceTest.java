package com.emartplus.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.emartplus.EmartplusApplication;
import com.emartplus.entity.RefreshToken;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.repository.RefreshTokenRepository;

@SpringBootTest(classes = EmartplusApplication.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L);
    }

    @Test
    void createRefreshToken_ValidUser_ReturnsRefreshToken() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Assert
        assertNotNull(refreshToken);
        assertEquals(user, refreshToken.getUser());
        assertNotNull(refreshToken.getToken());
        assertTrue(refreshToken.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void verifyExpiration_ValidToken_ReturnsRefreshToken() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusMillis(86400000));

        // Act
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(token);

        // Assert
        assertEquals(token, verifiedToken);
    }

    @Test
    void verifyExpiration_ExpiredToken_ThrowsApiException() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(1));

        // Act & Assert
        assertThrows(ApiException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository).delete(token);
    }
} 