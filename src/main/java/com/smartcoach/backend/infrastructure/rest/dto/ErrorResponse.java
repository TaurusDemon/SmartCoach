package com.smartcoach.backend.infrastructure.rest.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String message, int status, LocalDateTime timestamp) {
}
