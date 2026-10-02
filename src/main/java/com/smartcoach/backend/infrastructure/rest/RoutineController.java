package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.CreateRoutineUseCase;
import com.smartcoach.backend.application.usecase.GenerateWorkoutRoutineUseCase;
import com.smartcoach.backend.application.usecase.GetRoutinesByUserIdCase;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.infrastructure.rest.dto.*;
import com.smartcoach.backend.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/routines")
@Tag(name = "Rutinas", description = "Endpoints para la gestión de rutinas")
public class RoutineController {
    private final CreateRoutineUseCase createRoutineUseCase;
    private final GetRoutinesByUserIdCase getRoutinesByUserIdCase;
    private final GenerateWorkoutRoutineUseCase generateWorkoutRoutineUseCase;

    public RoutineController(CreateRoutineUseCase createRoutineUseCase, GetRoutinesByUserIdCase getRoutinesByUserIdCase, GenerateWorkoutRoutineUseCase generateWorkoutRoutineUseCase) {
        this.createRoutineUseCase = createRoutineUseCase;
        this.getRoutinesByUserIdCase = getRoutinesByUserIdCase;
        this.generateWorkoutRoutineUseCase = generateWorkoutRoutineUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar una nueva rutina")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rutina creada exitosamente",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RoutineResponse> createRoutine(@RequestBody @Valid CreateRoutineRequest createRoutineRequest) {

        List<CreateRoutineUseCase.ExerciseInput> exerciseInputList = createRoutineRequest.exercises().stream().map(e -> new CreateRoutineUseCase.ExerciseInput(e.name(),e.category(),e.sets(),e.reps(),e.restSeconds())).toList();
        Routine routine = createRoutineUseCase.execute(AuthenticatedUser.getAuthenticatedUserId(),createRoutineRequest.name(), createRoutineRequest.goal(), exerciseInputList);
        RoutineResponse response = RoutineResponse.fromDomain(routine);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener todas las rutinas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de rutinas obtenida exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoutineResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido o ausente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping()
    public ResponseEntity<List<RoutineResponse>> getRoutines() {

        List<Routine> routines = getRoutinesByUserIdCase.execute(AuthenticatedUser.getAuthenticatedUserId());
        List<RoutineResponse> routineResponseList = routines.stream().map(r -> RoutineResponse.fromDomain(r)).toList();

        return ResponseEntity.status(HttpStatus.OK).body(routineResponseList);
    }

    @PostMapping("/generate")
    public ResponseEntity<RoutineResponse> generateRoutine(@RequestBody(required = false) String additionalPreferences) {

        Routine routine = generateWorkoutRoutineUseCase.execute(AuthenticatedUser.getAuthenticatedUserId(), additionalPreferences);
        RoutineResponse response = RoutineResponse.fromDomain(routine);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
