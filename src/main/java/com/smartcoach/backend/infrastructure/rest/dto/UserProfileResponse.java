package com.smartcoach.backend.infrastructure.rest.dto;

import com.smartcoach.backend.domain.vo.ActivityLevel;
import com.smartcoach.backend.domain.vo.FitnessGoal;

import java.util.UUID;

public record UserProfileResponse(UUID userId, int age, double weight, int height, ActivityLevel activityLevel, FitnessGoal fitnessGoal) {
}
