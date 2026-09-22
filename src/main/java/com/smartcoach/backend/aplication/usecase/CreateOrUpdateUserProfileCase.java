package com.smartcoach.backend.aplication.usecase;

import com.smartcoach.backend.domain.expection.UserNotFoundException;
import com.smartcoach.backend.domain.model.ActivityLevel;
import com.smartcoach.backend.domain.model.FitnessGoal;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.Weight;

import java.util.Objects;
import java.util.UUID;

public class CreateOrUpdateUserProfileCase {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public CreateOrUpdateUserProfileCase(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.userProfileRepository = Objects.requireNonNull(userProfileRepository);
    }

    public UserProfile execute(UUID userId, int rawAge, double rawWeight, int rawHeight, ActivityLevel activityLevel, FitnessGoal fitnessGoal) {
        if(userRepository.findById(userId).isEmpty()){
            throw new UserNotFoundException(userId);
        }
        Age age = new Age(rawAge);
        Weight weight = new Weight(rawWeight);
        Height height = new Height(rawHeight);
        UserProfile userProfile = new UserProfile(userId, age, weight, height,activityLevel,fitnessGoal);

        return userProfileRepository.save(userProfile);
    }
}

