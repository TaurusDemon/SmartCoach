package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.Weight;

import java.util.Objects;
import java.util.UUID;

public class UserProfile {
    private final UUID userId;
    private final Age age;
    private final Weight weight;
    private final Height height;
    private final ActivityLevel activityLevel;
    private final FitnessGoal fitnessGoal;

    public UserProfile(UUID id, Age age, Weight weight, Height height, ActivityLevel activityLevel, FitnessGoal fitnessGoal) {
        this.userId = Objects.requireNonNull(id,"El id no puede ser nulo");
        this.age = Objects.requireNonNull(age,"La edad no puede ser nula");
        this.weight = Objects.requireNonNull(weight,"El peso no puede ser nulo");
        this.height = Objects.requireNonNull(height,"La altura no puede ser nula");
        this.activityLevel = Objects.requireNonNull(activityLevel,"El nivel de actividad no puede ser nulo");
        this.fitnessGoal = Objects.requireNonNull(fitnessGoal,"El objetivo no puede ser nulo");
    }

    public Age getAge() {
        return age;
    }

    public Weight getWeight() {
        return weight;
    }

    public Height getHeight() {
        return height;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public UUID getUserId() {
        return userId;
    }
}
