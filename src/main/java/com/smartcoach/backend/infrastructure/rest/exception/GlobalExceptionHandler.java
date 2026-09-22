package com.smartcoach.backend.infrastructure.rest.exception;

import com.smartcoach.backend.domain.expection.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.expection.UserNotFoundException;
import com.smartcoach.backend.infrastructure.rest.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. Captura la regla de negocio: Usuario/Email ya existente -> 409 CONFLICT
    @ExceptionHandler(UserAlreadyExistsExpection.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsExpection(UserAlreadyExistsExpection ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 2. Captura validaciones de Value Objects (ej. Email inválido) -> 400 BAD REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 3. Captura fallos de las anotaciones DTO (@NotBlank en RegisterUserRequest) -> 400 BAD REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String defaultMessage = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Error de validación en los datos enviados";

        ErrorResponse error = new ErrorResponse(
                defaultMessage,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 4. Captura validaciones de User Not Found -> 404 NOT FOUND
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
