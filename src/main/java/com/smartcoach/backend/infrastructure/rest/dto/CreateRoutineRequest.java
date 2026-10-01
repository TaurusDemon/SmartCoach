package com.smartcoach.backend.infrastructure.rest.dto;

import com.smartcoach.backend.domain.vo.RoutineGoal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoutineRequest (
        @NotNull(message = "El nombre de la rutina no puede ser nulo")
        String name,

        @NotNull(message = "El objetivo de la rutina es obligatorio")
        RoutineGoal goal,

        @NotEmpty(message = "La rutina debe incluir al menos un ejercicio")
        @Size(max = 15, message = "La rutina no puede tener más de 15 ejercicios")
        @Valid
        List<ExerciseRequest> exercises
){
}
