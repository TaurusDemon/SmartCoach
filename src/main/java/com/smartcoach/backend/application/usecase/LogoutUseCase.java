package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.TokenException;
import com.smartcoach.backend.domain.model.RefreshToken;
import com.smartcoach.backend.domain.port.RefreshTokenHasher;
import com.smartcoach.backend.domain.port.RefreshTokenRepository;
import com.smartcoach.backend.domain.vo.TokenStatus;

import java.util.UUID;

public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;


    public  LogoutUseCase(RefreshTokenRepository refreshTokenRepository, RefreshTokenHasher hasher) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHasher = hasher;
    }

    public void execute(String token) {

        String hash = refreshTokenHasher.hash(token);

        refreshTokenRepository.findById(hash).filter(t->t.getStatus() == TokenStatus.ACTIVE).ifPresent(t -> refreshTokenRepository.save(t.revoke()));
    }
}
