package com.smartcoach.backend.domain.vo;

public record PasswordHash(String value) {
    public PasswordHash{
        if (value == null || value.isEmpty()){
            throw new IllegalArgumentException("La hash no puede ser nula");
        }
    }
}
