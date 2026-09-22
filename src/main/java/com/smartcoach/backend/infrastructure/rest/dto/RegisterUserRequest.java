package com.smartcoach.backend.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest (
        @NotBlank (message = "El nombre es obligatorio")
        String name,
        @NotBlank (message = "El email es obligatorio")
        String email

){}
