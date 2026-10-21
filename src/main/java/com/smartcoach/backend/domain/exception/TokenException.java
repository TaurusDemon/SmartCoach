package com.smartcoach.backend.domain.exception;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super("Error en el Refresh Token: "+ message);
    }
}
