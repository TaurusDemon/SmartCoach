package com.smartcoach.backend.domain.exception;

import java.util.UUID;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException(UUID routineId) {

        super("No existe rutina con el id: "+ routineId);
    }
}
