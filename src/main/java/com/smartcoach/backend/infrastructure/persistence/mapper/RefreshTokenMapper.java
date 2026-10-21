package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.RefreshToken;
import com.smartcoach.backend.infrastructure.persistence.entity.RefreshTokenEntity;

public class RefreshTokenMapper {

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(entity.getHashToken(), entity.getUserId(),entity.getExpiryDate(),entity.getStatus());
    }

    public static RefreshTokenEntity toEntity(RefreshToken domain) {
        return new RefreshTokenEntity(domain.getHashToken(), domain.getUserId(), domain.getExpiryDate(), domain.getStatus());
    }
}
