package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.RefreshToken;
import com.smartcoach.backend.domain.port.RefreshTokenRepository;
import com.smartcoach.backend.domain.vo.TokenStatus;
import com.smartcoach.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    public JpaRefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {

        RefreshTokenEntity refreshTokenEntity = RefreshTokenMapper.toEntity(refreshToken);

        RefreshTokenEntity refreshTokenSaved = springDataRefreshTokenRepository.save(refreshTokenEntity);

        return RefreshTokenMapper.toDomain(refreshTokenSaved);
    }

    @Override
    public Optional<RefreshToken> findById(String token) {

        Optional<RefreshTokenEntity> refreshTokenEntity = springDataRefreshTokenRepository.findById(token);

        return refreshTokenEntity.map(RefreshTokenMapper::toDomain);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        springDataRefreshTokenRepository.revokeAllByUserId(userId, TokenStatus.ACTIVE, TokenStatus.REVOKED);
    }
}
