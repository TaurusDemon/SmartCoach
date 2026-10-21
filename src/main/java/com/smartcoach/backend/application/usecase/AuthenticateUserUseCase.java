package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.model.RefreshToken;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.*;
import com.smartcoach.backend.domain.vo.AuthTokens;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.TokenStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort,
                                   TokenProviderPort tokenProviderPort, RefreshTokenRepository refreshTokenRepository, RefreshTokenHasher refreshTokenHasher) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenHasher = refreshTokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthTokens execute(String email, String password){

        User user = userRepository.findByEmail(new Email(email)).orElseThrow(()-> new IllegalArgumentException("El email no existe en el sistema"));

        if(!passwordEncoderPort.matches(password, user.getPassword().value())){
            throw new IllegalArgumentException("Password incorrecto");
        }
        String refreshTokenString = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(refreshTokenHasher.hash(refreshTokenString), user.getId(), LocalDateTime.now().plusWeeks(1), TokenStatus.ACTIVE);
        refreshTokenRepository.save(refreshToken);

        String accessToken = tokenProviderPort.generateToken(user.getId(), user.getEmail().value(), user.getRole().name());

        return new AuthTokens(accessToken, refreshTokenString);
    }
}
