package com.smartcoach.backend.domain.port;

public interface RefreshTokenHasher {
    String hash(String token);
}
