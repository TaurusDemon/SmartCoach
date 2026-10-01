package com.smartcoach.backend.domain.vo;

public record ExcerciseDetails(int sets, int reps, int restSeconds) {
    public ExcerciseDetails{
        if (sets <= 0)
            throw new IllegalArgumentException("Las series no pueden ser 0");
        if(reps <= 0)
            throw new IllegalArgumentException("Las repeticiones no pueden ser 0");
        if(restSeconds <= 0)
            throw new IllegalArgumentException("El tiempo de descanso no puede ser 0");
    }
}
