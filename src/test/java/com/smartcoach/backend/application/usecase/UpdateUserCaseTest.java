package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.ForbiddenOperationException;
import com.smartcoach.backend.domain.exception.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.PasswordEncoderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * {@link UpdateUserCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link UserRepository} y
 * {@link PasswordEncoderPort}); el {@link User} y los Value Objects se
 * instancian reales dentro del SUT.
 */
@ExtendWith(MockitoExtension.class)
class UpdateUserCaseTest {

    private static final String RAW_PASSWORD = "MiClaveSegura123";
    private static final String ENCODED_PASSWORD = "$2a$10$abcdefghijklmnopqrstuv";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private UpdateUserCase updateUserCase;

    @BeforeEach
    void setUp() {
        updateUserCase = new UpdateUserCase(userRepository, passwordEncoderPort);
    }

    @Test
    @DisplayName("Debe actualizar y guardar el usuario conservando el mismo id cuando los datos son validos")
    void deberiaActualizarYGuardarElUsuario_cuandoElUsuarioExisteYLosDatosSonValidos() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByEmail(new Email("carlos.nuevo@ejemplo.com"))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        User resultado = updateUserCase.execute(userId, userId,"USER","Carlos Gomez Perez", "carlos.nuevo@ejemplo.com", RAW_PASSWORD);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(userId);
        assertThat(resultado.getName()).isEqualTo("Carlos Gomez Perez");
        assertThat(resultado.getEmail().value()).isEqualTo("carlos.nuevo@ejemplo.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(userId);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Debe persistir la contraseña cifrada y nunca la contraseña en texto plano")
    void deberiaPersistirLaContrasenaCifrada_cuandoElUsuarioSeActualiza() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        User resultado = updateUserCase.execute(userId, userId,"USER","Carlos Gomez Perez", "carlos.nuevo@ejemplo.com", RAW_PASSWORD);

        // Assert (Then)
        assertThat(resultado.getPassword().value()).isEqualTo(ENCODED_PASSWORD);
        assertThat(resultado.getPassword().value()).isNotEqualTo(RAW_PASSWORD);

        verify(passwordEncoderPort, times(1)).encode(RAW_PASSWORD);
    }

    @Test
    @DisplayName("Debe actualizar el usuario sin conflicto cuando el email ya le pertenece a si mismo")
    void deberiaActualizarElUsuario_cuandoElEmailNoCambia() {
        // Test de regresion del defecto original: la unicidad debe excluir el propio id,
        // de lo contrario un usuario no podria editar sus datos sin cambiar de email.
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuarioExistente = usuarioValido(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByEmail(new Email("carlos@ejemplo.com"))).thenReturn(Optional.of(usuarioExistente));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When): se cambia el nombre pero se conserva el email original.
        User resultado = updateUserCase.execute(userId, userId,"USER","Carlos Gomez Perez", "carlos@ejemplo.com", RAW_PASSWORD);

        // Assert (Then)
        assertThat(resultado.getEmail().value()).isEqualTo("carlos@ejemplo.com");
        assertThat(resultado.getName()).isEqualTo("Carlos Gomez Perez");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsExpection y no guardar cuando el email ya pertenece a otro usuario")
    void deberiaLanzarUserAlreadyExistsExpection_cuandoElEmailYaPerteneceAOtroUsuario() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID otroUserId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByEmail(new Email("carlos@ejemplo.com")))
                .thenReturn(Optional.of(usuarioValido(otroUserId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateUserCase.execute(userId, userId, "USER", "Impostor", "carlos@ejemplo.com", RAW_PASSWORD))
                .isInstanceOf(UserAlreadyExistsExpection.class)
                .hasMessageContaining("Ya existe un usuario registrado con el email: Email[value=carlos@ejemplo.com]");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException y no cifrar ni guardar cuando el usuario no existe")
    void deberiaLanzarUserNotFoundException_cuandoElUsuarioNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateUserCase.execute(userId, userId, "USER", "Carlos", "carlos@ejemplo.com", RAW_PASSWORD))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con el ID: " + userId);

        verify(passwordEncoderPort, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar cuando el email tiene formato invalido")
    void deberiaLanzarExcepcionDeNegocio_cuandoElEmailEsInvalido() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then): el VO Email falla antes de cifrar la contraseña.
        assertThatThrownBy(() -> updateUserCase.execute(userId, userId, "USER","Carlos", "email-sin-arroba", RAW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email no tiene un formato válido");

        verify(passwordEncoderPort, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar cuando la contraseña en texto plano es corta")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaContrasenaEsCorta() {
        // Arrange (Given): el VO Password valida el texto plano, no el hash.
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(passwordEncoderPort.encode("corta")).thenReturn(ENCODED_PASSWORD);

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateUserCase.execute(userId,userId, "USER", "Carlos", "carlos@ejemplo.com", "corta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La contraseña debe tener al menos 8 caracteres");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar ForbiddenOperationException y no guardar cuando quiere modificar otro usuario sin ser ADMIN")
    void deberiaLanzarExcepcionDeProhibicion_cuandoQuiereModificarOtroUsuarioSinAdmin() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateUserCase.execute(userId,targetId, "USER", "Carlos", "carlos@ejemplo.com", "corta"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("No tienes permisos para hacer esta operación");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private User usuarioValido(UUID userId) {
        return new User(userId, "Carlos Gomez", new Email("carlos@ejemplo.com"), new PasswordHash("contrasena123"));
    }
}