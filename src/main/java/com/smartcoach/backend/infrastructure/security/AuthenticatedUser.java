package com.smartcoach.backend.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class AuthenticatedUser {

    private AuthenticatedUser() {};

    public static UUID getAuthenticatedUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        
        return (UUID) authentication.getPrincipal();
    }

    public static String getUserRole(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        return  authorities.stream().map(GrantedAuthority::getAuthority).filter(Objects::nonNull).filter(rol -> rol.startsWith("ROLE_")).map(rol -> rol.replace("ROLE_", "")).findFirst().orElse("USER");
    }
}
