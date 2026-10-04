package com.smartcoach.backend.infrastructure.ai.dto;

import com.smartcoach.backend.domain.vo.RoutineGoal;
import com.smartcoach.backend.infrastructure.rest.dto.ExerciseResponse;

import java.time.LocalDateTime;
import java.util.List;

public record RoutineAiResponse(
        String name,
        RoutineGoal goal,
        List<ExerciseAiResponse> exercises
) {
}
