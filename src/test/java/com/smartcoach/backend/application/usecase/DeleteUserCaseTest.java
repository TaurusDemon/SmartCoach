package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.PasswordHash;
import com.smartcoach.backend.domain.vo.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link DeleteUserCase}.
 *
 * Solo se mockea el puerto de salida {@link UserRepository}; el {@link User} se
 * construye como entidad real del dominio.
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserCaseTest {

    @Mock
    private UserRepository userRepository;

    private DeleteUserCase deleteUserCase;

    @BeforeEach
    void setUp() {
        deleteUserCase = new DeleteUserCase(userRepository);
    }

    @Test
    @DisplayName("Debe eliminar el usuario cuando el id existe en el repositorio")
    void deberiaEliminarElUsuario_cuandoElIdExisteEnElRepositorio() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        String role = "USER";
        User usuarioExistente = usuarioValido(userId, "Carlos Gomez", "carlos@ejemplo.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));

        // Act (When)
        assertThatCode(() -> deleteUserCase.execute(userId, role, userId)).doesNotThrowAnyException();

        // Assert (Then)
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException y no eliminar cuando el id no existe")
    void deberiaLanzarUserNotFoundException_cuandoElIdNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        String role = "USER";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> deleteUserCase.execute(userId,role,userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con el ID: " + userId);

        // Invariante: nunca se intenta borrar un id inexistente.
        verify(userRepository, never()).deleteById(any());
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private User usuarioValido(UUID userId, String name, String email) {
        return new User(userId, name, new Email(email), new PasswordHash("contrasena123"));
    }
}