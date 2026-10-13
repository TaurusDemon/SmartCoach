package com.smartcoach.backend.infrastructure.security;

import com.smartcoach.backend.domain.port.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;


@Service
public class JwtService implements TokenProviderPort {

    private final SecretKey key;
    private final long expirationMs;

    //Lee las propiedades de aplication.yml
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        //Convierte la jwt.secret en una clave criptografica
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    //Genera un Token JWT con el ID y Email del usuario
    public String generateToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder().subject(userId.toString())
                .claim("email",email)
                .claim("role",role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    //Extrae el ID del usuario desde un token
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    //Extrae el Rol del usuario desde un token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role").toString();
    }

    //Compruba si el token es valido y no ha caducado
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
