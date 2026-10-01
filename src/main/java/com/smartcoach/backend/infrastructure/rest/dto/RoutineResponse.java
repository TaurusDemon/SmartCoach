package com.smartcoach.backend.infrastructure.rest.dto;
import com.smartcoach.backend.domain.model.Routine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoutineResponse(
        UUID id,
        UUID userId,
        String name,
        String goal,
        List<ExerciseResponse> exercises,
        LocalDateTime createdAt
) {
    public static RoutineResponse fromDomain(Routine domain) {
        return new RoutineResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getName(),
                domain.getRoutineGoal().name(),
                domain.getExerciseList().stream().map(ExerciseResponse::fromDomain).toList(),
                domain.getCreatedAt()
        );
    }
}