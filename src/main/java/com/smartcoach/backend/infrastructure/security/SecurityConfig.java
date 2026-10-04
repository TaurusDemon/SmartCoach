package com.smartcoach.backend.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JwtAuthenticatorFilter jwtAuthenticatorFilter;

    public  SecurityConfig(JwtAuthenticatorFilter jwtAuthenticatorFilter) {
        this.jwtAuthenticatorFilter = jwtAuthenticatorFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (No se necesita en APIs REST Stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Definir la política de sesión como STATELESS (sin cookies de sesión)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Reglas de autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas de Swagger UI / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api/v1/users"
                        ).permitAll()

                        // Rutas públicas de Autenticación (Login, Registro)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        //protegemos endpoints
                        .anyRequest().authenticated()
                )
                // Colocamos nuestro filtro antes del filtro estándar de Spring
                .addFilterBefore(jwtAuthenticatorFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}