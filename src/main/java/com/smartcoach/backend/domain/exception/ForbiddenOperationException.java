package com.smartcoach.backend.domain.exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException() {
        super("No tienes permisos para hacer esta operacion");
    }
}
