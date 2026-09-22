package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.infrastructure.persistence.entity.UserProfileEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataUserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository springDataUserProfileRepository;
    public  JpaUserProfileRepositoryAdapter(SpringDataUserProfileRepository springDataUserProfileRepository) {
        this.springDataUserProfileRepository = springDataUserProfileRepository;
    }
    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity userProfileEntityToSave = UserProfilePersistenceMapper.toEntity(userProfile);

        UserProfileEntity userProfileEntitySaved = springDataUserProfileRepository.save(userProfileEntityToSave);

        return UserProfilePersistenceMapper.toDomain(userProfileEntitySaved);
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        Optional<UserProfileEntity> userProfileEntity = springDataUserProfileRepository.findByUserId(userId);
        return userProfileEntity.map(UserProfilePersistenceMapper::toDomain);
    }
}
