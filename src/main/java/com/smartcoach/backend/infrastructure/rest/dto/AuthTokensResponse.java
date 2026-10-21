package com.smartcoach.backend.infrastructure.rest.dto;

public record AuthTokensResponse(String refreshToken, String accessToken) {
}
