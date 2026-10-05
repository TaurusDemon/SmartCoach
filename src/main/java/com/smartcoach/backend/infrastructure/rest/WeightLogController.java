package com.smartcoach.backend.infrastructure.rest;

import com.smartcoach.backend.application.usecase.CreateWeightLogUseCase;
import com.smartcoach.backend.application.usecase.GetWeightLogHistoryUseCase;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.infrastructure.rest.dto.ErrorResponse;
import com.smartcoach.backend.infrastructure.rest.dto.RoutineResponse;
import com.smartcoach.backend.infrastructure.rest.dto.WeightLogRequest;
import com.smartcoach.backend.infrastructure.rest.dto.WeightLogResponse;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/weighlog")
@Tag(name = "Seguimiento de pesos", description = "Endpoints para la gestión de seguimiento de peso")
public class WeightLogController {

    private final CreateWeightLogUseCase createWeightLogUseCase;
    private final GetWeightLogHistoryUseCase getWeightLogHistoryUseCase;

    public  WeightLogController(CreateWeightLogUseCase createWeightLogUseCase, GetWeightLogHistoryUseCase getWeightLogHistoryUseCase) {
        this.createWeightLogUseCase = createWeightLogUseCase;
        this.getWeightLogHistoryUseCase = getWeightLogHistoryUseCase;
    }


    @PostMapping
    @Operation(summary = "Registrar una nuevo segumiento de peso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seguimiento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WeightLogResponse> createWeightLog(@RequestBody WeightLogRequest weightLogRequest) {

        WeightLog weightLog = createWeightLogUseCase.execute(AuthenticatedUser.getAuthenticatedUserId(),weightLogRequest.weight(),weightLogRequest.notes());
        WeightLogResponse response = new WeightLogResponse(weightLog.getId(),weightLog.getUserId(), weightLog.getWeight().value(), weightLog.getLogDate(), weightLog.getNotes());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Obtener historial de seguimientos")
    public ResponseEntity<List<WeightLogResponse>> getWeightLogs(){

        List<WeightLog> weightLogList = getWeightLogHistoryUseCase.execute(AuthenticatedUser.getAuthenticatedUserId());
        List<WeightLogResponse> responseList = weightLogList.stream().map(w-> new WeightLogResponse(w.getId(),w.getUserId(),w.getWeight().value(),w.getLogDate(),w.getNotes())).toList();

        return ResponseEntity.status(HttpStatus.OK).body(responseList);
    }



}
