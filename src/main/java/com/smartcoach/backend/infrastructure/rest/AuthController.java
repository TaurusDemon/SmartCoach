package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.AuthenticateUserUseCase;
import com.smartcoach.backend.application.usecase.RegisterUserCase;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.infrastructure.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Registro y login", description = "Endpoints para la gestión del registro y login")
public class AuthController {

    private final RegisterUserCase registerUserCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserCase registerUserCase, AuthenticateUserUseCase authenticateUserUseCase){
        this.registerUserCase = registerUserCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registro del usuario", description = "Registro de un usuario con nombre, email y contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos o email inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest registerUserRequest){
        User user = registerUserCase.execute(registerUserRequest.name(), registerUserRequest.email(), registerUserRequest.password());

        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail().value());

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login del usuario", description = "Login de un usuario con email y contraseña que devuelve un token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario logado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos o email inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest){
       String token = authenticateUserUseCase.execute(authRequest.email(), authRequest.password());

       AuthResponse authResponse = new AuthResponse(token);

       return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }
}
