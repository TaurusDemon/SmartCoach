package com.smartcoach.backend.infrastructure.rest.dto;

import com.smartcoach.backend.domain.vo.ExcerciseDetails;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseRequest(@NotBlank(message = "El nombre del ejercicio no puede estar vacío")
                              String name,

                              @NotNull(message = "La categoría del ejercicio es obligatoria")
                              ExerciseCategory category,

                              @Min(value = 1, message = "Debe haber al menos 1 serie")
                              int sets,

                              @Min(value = 1, message = "Debe haber al menos 1 repetición")
                              int reps,

                              @Min(value = 0, message = "El tiempo de descanso no puede ser negativo")
                              int restSeconds) {
}
