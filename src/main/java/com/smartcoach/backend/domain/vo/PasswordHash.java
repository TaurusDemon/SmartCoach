package com.smartcoach.backend.domain.vo;

import java.util.Objects;

public record PasswordHash(String value) {
    public PasswordHash {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña no puede ser nulo ni vacío");
        }
    }
}