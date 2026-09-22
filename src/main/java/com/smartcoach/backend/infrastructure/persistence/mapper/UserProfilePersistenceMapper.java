package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.Weight;
import com.smartcoach.backend.infrastructure.persistence.entity.UserEntity;
import com.smartcoach.backend.infrastructure.persistence.entity.UserProfileEntity;

public class UserProfilePersistenceMapper {

    public static UserProfileEntity toEntity(UserProfile userProfile) {

        UserProfileEntity userProfileEntity = new UserProfileEntity();
        userProfileEntity.setAge(userProfile.getAge().value());
        userProfileEntity.setWeight(userProfile.getWeight().value());
        userProfileEntity.setHeight(userProfile.getHeight().valueCm());
        userProfileEntity.setActivityLevel(userProfile.getActivityLevel());
        userProfileEntity.setFitnessGoal(userProfile.getFitnessGoal());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userProfile.getUserId());
        userProfileEntity.setUser(userEntity);

        return userProfileEntity;
    }

    public static UserProfile toDomain(UserProfileEntity userProfileEntity){
        return new UserProfile(userProfileEntity.getUserId(), new Age(userProfileEntity.getAge()), new Weight(userProfileEntity.getWeight()), new Height(userProfileEntity.getHeight()), userProfileEntity.getActivityLevel(), userProfileEntity.getFitnessGoal());
    }
}
