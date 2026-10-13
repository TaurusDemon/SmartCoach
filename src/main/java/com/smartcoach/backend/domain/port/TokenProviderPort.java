package com.smartcoach.backend.domain.port;

import java.util.UUID;

public interface TokenProviderPort {
    String generateToken(UUID userId, String email, String role);
}
