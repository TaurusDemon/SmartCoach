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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link GetUserByIdCase}.
 *
 * Solo se mockea el puerto de salida {@link UserRepository}; el {@link User} se
 * construye como entidad real del dominio.
 */
@ExtendWith(MockitoExtension.class)
class GetUserByIdCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserByIdCase getUserByIdCase;

    @BeforeEach
    void setUp() {
        getUserByIdCase = new GetUserByIdCase(userRepository);
    }

    @Test
    @DisplayName("Debe retornar el usuario cuando el id existe en el repositorio")
    void deberiaRetornarElUsuario_cuandoElIdExisteEnElRepositorio() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuarioExistente = usuarioValido(userId, "Carlos Gomez", "carlos@ejemplo.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));

        // Act (When)
        User resultado = getUserByIdCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(userId);
        assertThat(resultado.getName()).isEqualTo("Carlos Gomez");
        assertThat(resultado.getEmail().value()).isEqualTo("carlos@ejemplo.com");
        assertThat(resultado.getPassword().value()).isEqualTo("contrasena123");

        // Una sola consulta: el SUT resuelve con Optional.orElseThrow (sin doble findById).
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Debe retornar el mismo usuario que devuelve el repositorio")
    void deberiaRetornarLaMismaInstancia_delRepositorio_cuandoElIdExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuarioExistente = usuarioValido(userId, "Ana Ruiz", "ana@ejemplo.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));

        // Act (When)
        User resultado = getUserByIdCase.execute(userId);

        // Assert (Then): el caso de uso no transforma la entidad, solo la expone.
        assertThat(resultado).isSameAs(usuarioExistente);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException cuando el id no existe en el repositorio")
    void deberiaLanzarUserNotFoundException_cuandoElIdNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> getUserByIdCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con el ID: " + userId);

        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private User usuarioValido(UUID userId, String name, String email) {
        return new User(userId, name, new Email(email), new PasswordHash("contrasena123"));
    }
}