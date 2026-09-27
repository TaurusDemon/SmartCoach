package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.CreateOrUpdateUserProfileCase;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.infrastructure.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/users")
public class UserProfileController {

    private final CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase;

    public UserProfileController(CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase) {
        this.createOrUpdateUserProfileCase = createOrUpdateUserProfileCase;
    }

    @PostMapping("/{userId}/profile")
    @Operation(summary = "Registrar un nuevo perfil de usuario", description = "Crea o actualiza un perfil de usuario en el sistema de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<UserProfileResponse> createUserProfile(@PathVariable UUID userId, @RequestBody CreateUserProfileRequest createUserProfileRequest) {

        UserProfile userProfile = createOrUpdateUserProfileCase.execute(userId, createUserProfileRequest.age(),
                createUserProfileRequest.weight(),createUserProfileRequest.height(),
                createUserProfileRequest.activityLevel(),createUserProfileRequest.fitnessGoal());

        UserProfileResponse response = new UserProfileResponse(userProfile.getUserId(),userProfile.getAge().
                value(),userProfile.getWeight().value(),userProfile.getHeight().valueCm(),
                userProfile.getActivityLevel(),userProfile.getFitnessGoal());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
