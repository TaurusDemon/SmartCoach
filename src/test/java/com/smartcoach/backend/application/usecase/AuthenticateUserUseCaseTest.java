package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.PasswordEncoderPort;
import com.smartcoach.backend.domain.port.TokenProviderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link AuthenticateUserUseCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link UserRepository},
 * {@link PasswordEncoderPort}, {@link TokenProviderPort}); el {@link User} es una
 * entidad real del dominio.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private AuthenticateUserUseCase authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        authenticateUserUseCase = new AuthenticateUserUseCase(userRepository, passwordEncoderPort, tokenProviderPort);
    }

    @Test
    @DisplayName("Debe retornar un token cuando el email existe y la contraseña coincide")
    void deberiaRetornarUnToken_cuandoElEmailExisteYLaContrasenaCoincide() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuario = usuarioValido(userId, "Carlos Gomez", "carlos@ejemplo.com");

        when(userRepository.findByEmail(new Email("carlos@ejemplo.com"))).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("MiClaveSegura123", "contrasena123")).thenReturn(true);
        when(tokenProviderPort.generateToken(userId, "carlos@ejemplo.com")).thenReturn("jwt.firmado.valido");

        // Act (When)
        String token = authenticateUserUseCase.execute("carlos@ejemplo.com", "MiClaveSegura123");

        // Assert (Then)
        assertThat(token).isNotNull().isEqualTo("jwt.firmado.valido");

        verify(passwordEncoderPort, times(1)).matches("MiClaveSegura123", "contrasena123");
        verify(tokenProviderPort, times(1)).generateToken(userId, "carlos@ejemplo.com");
        // Una sola consulta: el SUT resuelve con Optional.orElseThrow (sin doble findByEmail).
        verify(userRepository, times(1)).findByEmail(new Email("carlos@ejemplo.com"));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no generar token cuando el email no existe en el sistema")
    void deberiaLanzarExcepcionDeNegocio_cuandoElEmailNoExisteEnElSistema() {
        // Arrange (Given)
        when(userRepository.findByEmail(new Email("fantasma@ejemplo.com"))).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> authenticateUserUseCase.execute("fantasma@ejemplo.com", "MiClaveSegura123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email no existe en el sistema");

        // Invariante de seguridad: sin usuario no se verifica contrasena ni se emite token.
        verify(passwordEncoderPort, never()).matches(any(), any());
        verify(tokenProviderPort, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no generar token cuando la contraseña no coincide")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaContrasenaNoCoincide() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuario = usuarioValido(userId, "Carlos Gomez", "carlos@ejemplo.com");

        when(userRepository.findByEmail(new Email("carlos@ejemplo.com"))).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("ContrasenaEquivocada", "contrasena123")).thenReturn(false);

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> authenticateUserUseCase.execute("carlos@ejemplo.com", "ContrasenaEquivocada"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password incorrecto");

        verify(tokenProviderPort, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no consultar el repositorio cuando el email tiene formato invalido")
    void deberiaLanzarExcepcionDeNegocio_cuandoElEmailEsInvalido() {
        // Act & Assert (When & Then): el VO Email falla antes de tocar el puerto.
        assertThatThrownBy(() -> authenticateUserUseCase.execute("email-sin-arroba", "MiClaveSegura123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email no tiene un formato válido");

        verify(userRepository, never()).findByEmail(any());
        verify(tokenProviderPort, never()).generateToken(any(), any());
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private User usuarioValido(UUID userId, String name, String email) {
        return new User(userId, name, new Email(email), new PasswordHash("contrasena123"));
    }
}