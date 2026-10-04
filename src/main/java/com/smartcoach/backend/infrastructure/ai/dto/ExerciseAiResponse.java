package com.smartcoach.backend.infrastructure.ai.dto;

import com.smartcoach.backend.domain.vo.ExerciseCategory;

public record ExerciseAiResponse(
        String name,
        ExerciseCategory category,
        int sets,
        int reps,
        int restSeconds
) {
}
