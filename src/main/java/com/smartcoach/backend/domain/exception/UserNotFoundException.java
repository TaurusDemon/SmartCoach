package com.smartcoach.backend.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{
    public  UserNotFoundException(UUID id) {
        super("Usuario no encontrado con el ID: "+ id);
    }
}
