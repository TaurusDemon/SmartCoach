package com.smartcoach.backend.infrastructure.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WeightLogResponse(
        UUID id,
        UUID userId,
        double weight,
        LocalDateTime logDate,
        String notes
){
}
