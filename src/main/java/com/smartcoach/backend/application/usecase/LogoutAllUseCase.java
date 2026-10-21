package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.port.RefreshTokenHasher;
import com.smartcoach.backend.domain.port.RefreshTokenRepository;
import com.smartcoach.backend.domain.vo.TokenStatus;

import java.util.UUID;

public class LogoutAllUseCase {

    private final RefreshTokenRepository refreshTokenRepository;


    public LogoutAllUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(UUID userId) {

        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
