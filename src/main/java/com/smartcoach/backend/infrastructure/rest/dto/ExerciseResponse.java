package com.smartcoach.backend.infrastructure.rest.dto;
import com.smartcoach.backend.domain.model.Exercise;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String category,
        int sets,
        int reps,
        int restSeconds
) {
    public static ExerciseResponse fromDomain(Exercise domain) {
        return new ExerciseResponse(
                domain.getId(),
                domain.getName(),
                domain.getExerciseCategory().name(),
                domain.getExerciseDetails().sets(),
                domain.getExerciseDetails().reps(),
                domain.getExerciseDetails().restSeconds()
        );
    }
}
