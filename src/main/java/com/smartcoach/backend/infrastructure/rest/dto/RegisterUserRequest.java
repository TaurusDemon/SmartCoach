package com.smartcoach.backend.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest (
        @NotBlank (message = "El nombre es obligatorio")
        String name,
        @NotBlank (message = "El email es obligatorio")
        String email,
        @NotBlank (message = "La contraseña no puede ser nula")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password

){}
