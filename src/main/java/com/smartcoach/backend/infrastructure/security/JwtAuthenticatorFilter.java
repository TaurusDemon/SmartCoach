package com.smartcoach.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticatorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticatorFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extraemos el encabezado HTTP llamado "Authorization"
        final String authorizationHeader = request.getHeader("Authorization");

        // 2. Si no hay encabezado o no empieza por "Bearer ", dejamos seguir la petición
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos solo el token (quitando la palabra "Bearer " de los primeros 7 caracteres)
        final String jwt = authorizationHeader.substring(7);

        // 4. Validamos el token y nos aseguramos de que el usuario no esté ya autenticado en el contexto
        if(jwtService.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String userIdStr = jwtService.extractUserId(jwt);
            UUID userId = UUID.fromString(userIdStr);

            // 5. Creamos el objeto de autenticación de Spring Security con el UUID del usuario
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. Le decimos a Spring Security: "Este usuario está autenticado"
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

}
