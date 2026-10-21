package com.smartcoach.backend.infrastructure.persistence.repository;

import com.smartcoach.backend.domain.vo.TokenStatus;
import com.smartcoach.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity,String> {
    Optional<RefreshTokenEntity> findById(String hashToken);
    @Transactional
    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.status = :revokedStatus WHERE t.userId = :userId AND t.status = :activeStatus")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("activeStatus")TokenStatus activeStatus, @Param("revokedStatus") TokenStatus revokedStatus);
}
