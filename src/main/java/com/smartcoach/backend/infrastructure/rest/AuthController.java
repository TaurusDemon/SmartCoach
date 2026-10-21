package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.*;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.vo.AuthTokens;
import com.smartcoach.backend.infrastructure.rest.dto.*;
import com.smartcoach.backend.infrastructure.security.AuthenticatedUser;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Registro y login", description = "Endpoints para la gestión del registro y login")
public class AuthController {

    private final RegisterUserCase registerUserCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;

    public AuthController(RegisterUserCase registerUserCase, AuthenticateUserUseCase authenticateUserUseCase, RefreshAccessTokenUseCase refreshAccessTokenUseCase, LogoutUseCase logoutUseCase, LogoutAllUseCase logoutAllUseCase) {
        this.registerUserCase = registerUserCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
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
                    content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos o email inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<AuthTokensResponse> login(@RequestBody @Valid AuthRequest authRequest){
       AuthTokens token = authenticateUserUseCase.execute(authRequest.email(), authRequest.password());

       AuthTokensResponse authResponse = new AuthTokensResponse(token.refreshToken(), token.accessToken());

       return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @Operation(summary = "Generar nuevos tokens", description = "Genera nuevos tokens tras caducar el Access Token usando el Refresh Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens creados",
                    content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token no válido o revocado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest){
        AuthTokens authTokens = refreshAccessTokenUseCase.execute(refreshTokenRequest.refreshToken());

        AuthTokensResponse response = new AuthTokensResponse(authTokens.refreshToken(), authTokens.accessToken());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "Logout de la sesión", description = "Revoca el Refresh Token para salir de la sesión")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sin contenido"),
    })
    @PostMapping("/logout")
    public HttpStatus logout(@RequestBody RefreshTokenRequest refreshTokenRequest){
        logoutUseCase.execute(refreshTokenRequest.refreshToken());

        return HttpStatus.NO_CONTENT;
    }

    @Operation(summary = "Logout de la todas las sesiones", description = "Revoca todos Refresh Token para salir de todas las sesiones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sin contenido"),
    })
    @PostMapping("/logout-all")
    public HttpStatus logoutAll(){
        UUID userId = AuthenticatedUser.getAuthenticatedUserId();

        logoutAllUseCase.execute(userId);
        return HttpStatus.NO_CONTENT;
    }
}
