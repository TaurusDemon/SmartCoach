package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * {@link CreateOrUpdateUserProfileCase}.
 *
 * Solo se mockean los puertos de salida ({@link UserRepository} y
 * {@link UserProfileRepository}); las entidades y Value Objects del dominio se
 * instancian con datos reales y validos para ejercitar el comportamiento real.
 */
@ExtendWith(MockitoExtension.class)
class CreateOrUpdateUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase;

    @BeforeEach
    void setUp() {
        // Inyeccion manual en el SUT: solo collaborators, nunca entidades de dominio.
        createOrUpdateUserProfileCase = new CreateOrUpdateUserProfileCase(userRepository, userProfileRepository);
    }

    @Test
    @DisplayName("Debe crear y guardar el perfil cuando el usuario existe y los VOs son validos")
    void deberiaCrearYGuardarElPerfil_cuandoElUsuarioExisteYLosVOsSonValidos() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        User usuarioExistente = usuarioValido(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        UserProfile resultado = createOrUpdateUserProfileCase.execute(
                userId, 28, 75.5, 178, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUserId()).isEqualTo(userId);
        assertThat(resultado.getAge().value()).isEqualTo(28);
        assertThat(resultado.getWeight().value()).isEqualTo(75.5);
        assertThat(resultado.getHeight().valueCm()).isEqualTo(178);
        assertThat(resultado.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(resultado.getFitnessGoal()).isEqualTo(FitnessGoal.MUSCLE_GAIN);

        verify(userRepository, times(1)).findById(userId);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe mapear los datos crudos a Value Objects del dominio al guardar el perfil")
    void deberiaMapearLosDatosCrudosAValueObjects_cuandoElPerfilEsValido() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        createOrUpdateUserProfileCase.execute(
                userId, 35, 92.0, 183, ActivityLevel.INTENSE, FitnessGoal.WEIGHT_LOSS);

        // Assert (Then): capturamos la entidad real enviada al puerto de salida.
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository, times(1)).save(profileCaptor.capture());

        UserProfile perfilPersistido = profileCaptor.getValue();
        assertThat(perfilPersistido.getUserId()).isEqualTo(userId);
        assertThat(perfilPersistido.getAge().value()).isEqualTo(35);
        assertThat(perfilPersistido.getWeight().value()).isEqualTo(92.0);
        assertThat(perfilPersistido.getHeight().valueCm()).isEqualTo(183);
        assertThat(perfilPersistido.getActivityLevel()).isEqualTo(ActivityLevel.INTENSE);
        assertThat(perfilPersistido.getFitnessGoal()).isEqualTo(FitnessGoal.WEIGHT_LOSS);
    }

    @Test
    @DisplayName("Debe actualizar el perfil cuando el usuario ya tenia uno registrado")
    void deberiaActualizarElPerfil_cuandoElUsuarioYaTienePerfilRegistrado() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfilPrevio = new UserProfile(userId,
                new Age(28), new Weight(75.5), new Height(178),
                ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        UserProfile resultado = createOrUpdateUserProfileCase.execute(
                userId, 29, 74.0, 178, ActivityLevel.INTENSE, FitnessGoal.MUSCLE_GAIN);

        // Assert (Then)
        assertThat(resultado.getUserId()).isEqualTo(perfilPrevio.getUserId());
        assertThat(resultado.getAge().value()).isEqualTo(29);
        assertThat(resultado.getWeight().value()).isEqualTo(74.0);
        assertThat(resultado.getActivityLevel()).isEqualTo(ActivityLevel.INTENSE);

        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException y no guardar perfil cuando el usuario no existe")
    void deberiaLanzarUserNotFoundException_cuandoElUsuarioNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, 28, 75.5, 178, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con el ID: " + userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @ParameterizedTest(name = "peso invalido = {0}")
    @ValueSource(doubles = {0.0, -10.0, -0.1, 300.5, 351.0})
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar perfil cuando el peso viola el invariante del VO Weight")
    void deberiaLanzarExcepcionDeNegocio_cuandoElPesoViolaElInvarianteDelVO(double pesoInvalido) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, 28, pesoInvalido, 178, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El peso no puede ser menor que 0 o mayor que 300");

        // La invariante del dominio corta el flujo antes de tocar la persistencia.
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @ParameterizedTest(name = "edad invalida = {0}")
    @ValueSource(ints = {0, 14, -5, 120, 150})
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar perfil cuando la edad viola el invariante del VO Age")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaEdadViolaElInvarianteDelVO(int edadInvalida) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, edadInvalida, 75.5, 178, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La edad no es válida");

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @ParameterizedTest(name = "altura invalida = {0}")
    @ValueSource(ints = {0, -180})
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar perfil cuando la altura viola el invariante del VO Height")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaAlturaViolaElInvarianteDelVO(int alturaInvalida) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, 28, 75.5, alturaInvalida, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La altura no puede ser menor que 0");

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException y no guardar perfil cuando el nivel de actividad es nulo")
    void deberiaLanzarExcepcionDeNegocio_cuandoElNivelDeActividadEsNulo() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, 28, 75.5, 178, null, FitnessGoal.MUSCLE_GAIN))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("El nivel de actividad no puede ser nulo");

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException y no guardar perfil cuando el objetivo de fitness es nulo")
    void deberiaLanzarExcepcionDeNegocio_cuandoElObjetivoDeFitnessEsNulo() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createOrUpdateUserProfileCase.execute(
                userId, 28, 75.5, 178, ActivityLevel.MODERATE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("El objetivo no puede ser nulo");

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private User usuarioValido(UUID userId) {
        return new User(userId, "Carlos Gomez", new Email("carlos@ejemplo.com"), new PasswordHash("contrasena123"));
    }
}