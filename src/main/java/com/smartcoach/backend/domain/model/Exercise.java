package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.ExerciseCategory;

import java.util.Objects;
import java.util.UUID;

public class Exercise {
    private final UUID id;
    private final String name;
    private final ExerciseCategory exerciseCategory;
    private final ExerciseDetails exerciseDetails;

    public Exercise(UUID id, String name, ExerciseCategory exerciseCategory, ExerciseDetails exerciseDetails) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.exerciseCategory = Objects.requireNonNull(exerciseCategory);
        this.exerciseDetails = Objects.requireNonNull(exerciseDetails);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ExerciseCategory getExerciseCategory() {
        return exerciseCategory;
    }

    public ExerciseDetails getExerciseDetails() {
        return exerciseDetails;
    }
}
