package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.TokenStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RefreshToken {

    private final String hashToken;
    private final UUID userId;
    private final LocalDateTime expiryDate;
    private final TokenStatus status;

    public RefreshToken(String hashToken, UUID userId, LocalDateTime expiryDate, TokenStatus status) {
        this.hashToken = Objects.requireNonNull(hashToken);
        this.userId = Objects.requireNonNull(userId);
        this.expiryDate = Objects.requireNonNull(expiryDate);
        this.status = Objects.requireNonNull(status);
    }

    public String getHashToken() {
        return hashToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public RefreshToken revoke() {
        return new RefreshToken(this.hashToken, this.userId, this.expiryDate, TokenStatus.REVOKED);
    }

}
