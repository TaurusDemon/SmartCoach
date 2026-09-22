package com.smartcoach.backend.infrastructure.rest.dto;

import com.smartcoach.backend.domain.model.ActivityLevel;
import com.smartcoach.backend.domain.model.FitnessGoal;

import java.util.UUID;

public record UserProfileResponse(UUID userId, int age, double weight, int height, ActivityLevel activityLevel, FitnessGoal fitnessGoal) {
}
