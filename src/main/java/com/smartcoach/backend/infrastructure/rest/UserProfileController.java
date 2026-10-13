package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.CreateOrUpdateUserProfileCase;
import com.smartcoach.backend.application.usecase.GetUserProfileByIdCase;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.infrastructure.rest.dto.*;
import com.smartcoach.backend.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/users")
@Tag(name = "Perfiles de usuario", description = "Endpoints para la gestión de perfiles de usuario")
public class UserProfileController {

    private final CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase;
    private final GetUserProfileByIdCase getUserProfileByIdCase;

    public UserProfileController(CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase, GetUserProfileByIdCase getUserProfileByIdCase) {
        this.createOrUpdateUserProfileCase = createOrUpdateUserProfileCase;
        this.getUserProfileByIdCase = getUserProfileByIdCase;
    }

    @PostMapping("/profile")
    @Operation(summary = "Registrar un nuevo perfil de usuario", description = "Crea o actualiza un perfil de usuario en el sistema de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<UserProfileResponse> createUserProfile(@RequestBody CreateUserProfileRequest createUserProfileRequest) {

        UUID userId = AuthenticatedUser.getAuthenticatedUserId();

        UserProfile userProfile = createOrUpdateUserProfileCase.execute(userId, createUserProfileRequest.age(),
                createUserProfileRequest.weight(),createUserProfileRequest.height(),
                createUserProfileRequest.activityLevel(),createUserProfileRequest.fitnessGoal());

        UserProfileResponse response = new UserProfileResponse(userProfile.getUserId(),userProfile.getAge().
                value(),userProfile.getWeight().value(),userProfile.getHeight().valueCm(),
                userProfile.getActivityLevel(),userProfile.getFitnessGoal());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Buscar un perfil de usuario", description = "Busca un perfil usuario en el sistema en en base a su ID de login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil de suario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El perfil de usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserProfileResponse> getUserProfile (){

        UserProfile userProfile = getUserProfileByIdCase.execute(AuthenticatedUser.getAuthenticatedUserId());

        UserProfileResponse response = new UserProfileResponse(userProfile.getUserId(), userProfile.getAge().value(), userProfile.getWeight().value(), userProfile.getHeight().valueCm(), userProfile.getActivityLevel(), userProfile.getFitnessGoal());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
