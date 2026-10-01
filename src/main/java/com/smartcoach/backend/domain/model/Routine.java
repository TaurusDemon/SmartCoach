package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.RoutineGoal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Routine {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final RoutineGoal routineGoal;
    private final List<Exercise> exerciseList;
    private final LocalDateTime createdAt;

    public Routine(UUID uuid, UUID userId, String name, RoutineGoal routineGoal, List<Exercise> exerciseList, LocalDateTime createdAt) {
        if (exerciseList == null || exerciseList.isEmpty()) {
            throw new IllegalArgumentException("La rutina debe contener al menos un ejercicio");
        }
        if (exerciseList.size() > 15) {
            throw new IllegalArgumentException("Una rutina no puede tener más de 15 ejercicios");
        }

        this.id = Objects.requireNonNull(uuid);
        this.userId = Objects.requireNonNull(userId);
        this.name = Objects.requireNonNull(name);
        this.routineGoal = Objects.requireNonNull(routineGoal);
        this.exerciseList = Objects.requireNonNull(exerciseList);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public RoutineGoal getRoutineGoal() {
        return routineGoal;
    }

    public List<Exercise> getExerciseList() {
        return exerciseList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
