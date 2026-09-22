package com.smartcoach.backend.infrastructure.rest.dto;

import com.smartcoach.backend.domain.model.ActivityLevel;
import com.smartcoach.backend.domain.model.FitnessGoal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserProfileRequest(
        @NotNull(message = "La edad es obligatoria")
        @Min(value = 15, message = "Debes ser mayor de 14 años")
        int age,
        @NotNull(message = "El peso es obligatorio")
        @Min(value = 0, message = "El peso no puede ser menor que 0")
        double weight,
        @NotNull(message = "La altura es obligatoria")
        @Min(value = 0, message = "La altura no puede ser menor que 0")
        int height,
        @NotBlank(message = "El nivel de actividad es obligatorio")
        ActivityLevel activityLevel,
        @NotBlank(message = "El objetivo de entrenamiento es obligatorio")
        FitnessGoal fitnessGoal

) {
}
