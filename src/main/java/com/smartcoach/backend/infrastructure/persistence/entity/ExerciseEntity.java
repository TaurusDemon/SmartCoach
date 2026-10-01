package com.smartcoach.backend.infrastructure.persistence.entity;

import com.smartcoach.backend.domain.vo.ExerciseCategory;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "exercises")
public class ExerciseEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ExerciseCategory category;

    @Column(nullable = false)
    private int sets;

    @Column(nullable = false)
    private int reps;

    @Column(name="rest_seconds", nullable = false)
    private int restSeconds;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name= "routine_id", nullable = false)
    private RoutineEntity routine;

    public ExerciseEntity(){}

    public ExerciseEntity(UUID id, String name, ExerciseCategory category, int sets, int reps, int restSeconds, RoutineEntity routine) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.routine = routine;
    }

    public String getName() {
        return name;
    }

    public ExerciseCategory getCategory() {
        return category;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public RoutineEntity getRoutine() {
        return routine;
    }

    public UUID getId() {
        return id;
    }

    public void setRoutine(RoutineEntity routine) {
        this.routine = routine;
    }
}
