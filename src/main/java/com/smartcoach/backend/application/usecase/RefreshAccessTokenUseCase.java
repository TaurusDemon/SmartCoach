package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.TokenException;
import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.RefreshToken;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.RefreshTokenHasher;
import com.smartcoach.backend.domain.port.RefreshTokenRepository;
import com.smartcoach.backend.domain.port.TokenProviderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.AuthTokens;
import com.smartcoach.backend.domain.vo.TokenStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshAccessTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    private final TokenProviderPort tokenProviderPort;
    private final UserRepository userRepository;

    public RefreshAccessTokenUseCase(RefreshTokenRepository refreshTokenRepository, RefreshTokenHasher refreshTokenHasher, TokenProviderPort tokenProviderPort, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHasher = refreshTokenHasher;
        this.tokenProviderPort = tokenProviderPort;
        this.userRepository = userRepository;
    }

    public AuthTokens execute(String token) {

        RefreshToken oldToken = refreshTokenRepository.findById(refreshTokenHasher.hash(token)).orElseThrow(()-> new TokenException("El token no existe."));
        UUID userId = oldToken.getUserId();

        if (oldToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenException("Refresh token inválido.");
        }

        if(oldToken.getStatus() == TokenStatus.REVOKED){
            refreshTokenRepository.revokeAllByUserId(userId);
            throw new TokenException("Refresh token inválido.");
        }

        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new TokenException("Refresh token inválido."));


        String tokenResponse = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken(refreshTokenHasher.hash(tokenResponse),user.getId(), LocalDateTime.now().plusWeeks(1),TokenStatus.ACTIVE);
        refreshTokenRepository.save(oldToken.revoke());
        refreshTokenRepository.save(newToken);

        String accessToken = tokenProviderPort.generateToken(userId, user.getEmail().value(), user.getRole().name());

        return new AuthTokens(accessToken, tokenResponse);

    }
}
