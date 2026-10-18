package com.smartcoach.backend.domain.exception;

import java.util.UUID;

public class WeightLogNotFoundException extends RuntimeException {
    public WeightLogNotFoundException(UUID id) {

        super("El usuario: "+ id +" no tiene ningún historial de peso");
    }
}
