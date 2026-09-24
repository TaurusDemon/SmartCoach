package com.smartcoach.backend.domain.exception;

public class UserAlreadyExistsExpection extends RuntimeException {
    public UserAlreadyExistsExpection(String email) {
        super("Ya existe un usuario registrado con el email: "+ email);
    }
}
