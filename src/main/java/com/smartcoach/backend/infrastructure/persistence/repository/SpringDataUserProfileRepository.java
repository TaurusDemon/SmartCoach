package com.smartcoach.backend.infrastructure.persistence.repository;

import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    Optional<UserProfileEntity> findByUserId(UUID userId);

}
