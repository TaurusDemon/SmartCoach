package com.smartcoach.backend.infrastructure.persistence.entity;

import com.smartcoach.backend.domain.vo.RoutineGoal;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class RoutineEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoutineGoal routineGoal;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ExerciseEntity> exerciseList = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RoutineEntity(){}

    public RoutineEntity(UUID id, UUID userId, String name, RoutineGoal routineGoal, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.routineGoal = routineGoal;
        this.createdAt = createdAt;
    }

    public void addExercise(ExerciseEntity exercise) {
        exerciseList.add(exercise);
        exercise.setRoutine(this);
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

    public List<ExerciseEntity> getExerciseList() {
        return exerciseList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

