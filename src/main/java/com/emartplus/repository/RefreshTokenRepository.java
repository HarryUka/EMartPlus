package com.emartplus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emartplus.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser_Id(Long userId);
} 