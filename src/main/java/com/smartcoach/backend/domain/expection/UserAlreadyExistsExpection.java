package com.smartcoach.backend.domain.expection;

public class UserAlreadyExistsExpection extends RuntimeException {
    public UserAlreadyExistsExpection(String email) {
        super("Ya existe un usuario registrado con el email: "+ email);
    }
}
