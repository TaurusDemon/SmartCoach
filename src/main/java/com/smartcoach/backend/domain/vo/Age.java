package com.smartcoach.backend.domain.vo;

public record Age(int value) {
    public Age{
        if(value <=14 || value >= 120)
            throw new IllegalArgumentException("La edad no es válida");
    }
}
