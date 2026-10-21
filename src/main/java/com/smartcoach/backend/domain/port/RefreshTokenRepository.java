package com.smartcoach.backend.domain.port;

import com.smartcoach.backend.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findById(String token);
    void revokeAllByUserId(UUID userId);
}
