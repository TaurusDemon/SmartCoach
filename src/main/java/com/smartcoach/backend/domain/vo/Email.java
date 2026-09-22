package com.smartcoach.backend.domain.vo;

public record Email(String value) {
    public Email {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }
}