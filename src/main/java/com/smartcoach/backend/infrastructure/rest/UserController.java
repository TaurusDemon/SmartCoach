package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.DeleteUserCase;
import com.smartcoach.backend.application.usecase.GetUserByIdCase;
import com.smartcoach.backend.application.usecase.RegisterUserCase;
import com.smartcoach.backend.application.usecase.UpdateUserCase;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.infrastructure.rest.dto.ErrorResponse;
import com.smartcoach.backend.infrastructure.rest.dto.RegisterUserRequest;
import com.smartcoach.backend.infrastructure.rest.dto.UserResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping ("/api/v1/users")
@Tag(name = "Usuarios", description = "Endpoints para la gestión e itinerario de usuarios")
public class UserController {

    private final GetUserByIdCase getUserByIdCase;
    private final UpdateUserCase updateUserCase;
    private final DeleteUserCase deleteUserCase;

    public UserController (GetUserByIdCase getUserByIdCase, UpdateUserCase updateUserCase, DeleteUserCase deleteUserCase) {
        this.getUserByIdCase = getUserByIdCase;
        this.updateUserCase = updateUserCase;
        this.deleteUserCase = deleteUserCase;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un usuario", description = "Busca un usuario en el sistema en en base a su ID pasado como parametro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> findUser(@PathVariable UUID id) {
        User userFound = getUserByIdCase.execute(id);
        UserResponse response = new UserResponse(userFound.getId(), userFound.getName(), userFound.getEmail().value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = "Actualiza un usuario en el sistema en en base a su ID pasado como parametro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody RegisterUserRequest registerUserRequest){
        User modifiedUser = updateUserCase.execute(id, AuthenticatedUser.getAuthenticatedUserId(), AuthenticatedUser.getUserRole(), registerUserRequest.name(), registerUserRequest.email(),registerUserRequest.password());
        UserResponse response = new UserResponse(modifiedUser.getId(), modifiedUser.getName(), modifiedUser.getEmail().value());

        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario en el sistema en en base a su ID pasado como parametro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permiso",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id){

        deleteUserCase.execute(id, AuthenticatedUser.getUserRole(), AuthenticatedUser.getAuthenticatedUserId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
