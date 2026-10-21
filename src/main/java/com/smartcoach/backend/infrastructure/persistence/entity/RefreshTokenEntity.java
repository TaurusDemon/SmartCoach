package com.smartcoach.backend.infrastructure.persistence.entity;

import com.smartcoach.backend.domain.vo.TokenStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    private String hashToken;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private TokenStatus status;

    public RefreshTokenEntity() {}

    public RefreshTokenEntity(String hashToken, UUID userId, LocalDateTime expiryDate, TokenStatus status) {
        this.hashToken = hashToken;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.status = status;
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

    public void setHashToken(String hashToken) {
        this.hashToken = hashToken;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
