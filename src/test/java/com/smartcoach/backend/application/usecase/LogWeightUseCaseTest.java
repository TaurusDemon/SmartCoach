package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.WeightLogRepository;
import com.smartcoach.backend.domain.vo.ActivityLevel;
import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.FitnessGoal;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
 * {@link CreateWeightLogUseCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link UserProfileRepository} y
 * {@link WeightLogRepository}); el {@link UserProfile} y el {@link WeightLog} son
 * objetos REALES del dominio.
 */
@ExtendWith(MockitoExtension.class)
class LogWeightUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private WeightLogRepository weightLogRepository;

    private CreateWeightLogUseCase logWeightUseCase;

    @BeforeEach
    void setUp() {
        logWeightUseCase = new CreateWeightLogUseCase(weightLogRepository, userProfileRepository);
    }

    @Test
    @DisplayName("Debe registrar el pesaje asociando el userId, el peso, las notas y la fecha actual")
    void deberiaRegistrarElPesaje_cuandoElUsuarioTienePerfilYElPesoEsValido() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        String notas = "Desayuno ligero";
        LocalDateTime antes = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.save(any(WeightLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        WeightLog resultado = logWeightUseCase.execute(userId, 74.8, notas);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getUserId()).isEqualTo(userId);
        assertThat(resultado.getWeight().value()).isEqualTo(74.8);
        assertThat(resultado.getNotes()).isEqualTo(notas);
        assertThat(resultado.getLogDate())
                .isAfterOrEqualTo(antes)
                .isBeforeOrEqualTo(LocalDateTime.now());

        verify(userProfileRepository, times(1)).findByUserId(userId);
        verify(weightLogRepository, times(1)).save(any(WeightLog.class));
    }

    @Test
    @DisplayName("Debe enviar al repositorio el WeightLog con el userId y el instante de registro correctos")
    void deberiaPersistirElWeightLogConUserIdYFechaActuales_cuandoElPesajeEsValido() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        LocalDateTime antes = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.save(any(WeightLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        logWeightUseCase.execute(userId, 80.0, "Nota de control semanal");

        // Assert (Then): capturamos la entidad real enviada al puerto de salida.
        ArgumentCaptor<WeightLog> weightLogCaptor = ArgumentCaptor.forClass(WeightLog.class);
        verify(weightLogRepository, times(1)).save(weightLogCaptor.capture());

        WeightLog persistido = weightLogCaptor.getValue();
        assertThat(persistido.getUserId()).isEqualTo(userId);
        assertThat(persistido.getWeight()).isEqualTo(new Weight(80.0));
        assertThat(persistido.getNotes()).isEqualTo("Nota de control semanal");
        assertThat(persistido.getLogDate())
                .isAfterOrEqualTo(antes)
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe registrar el pesaje con notas nulas cuando las notas son opcionales")
    void deberiaRegistrarElPesaje_cuandoLasNotasSonNulas() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.save(any(WeightLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        WeightLog resultado = logWeightUseCase.execute(userId, 70.0, null);

        // Assert (Then)
        assertThat(resultado.getNotes()).isNull();
        assertThat(resultado.getWeight().value()).isEqualTo(70.0);
        verify(weightLogRepository, times(1)).save(any(WeightLog.class));
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException y no registrar el pesaje cuando el usuario no tiene perfil")
    void deberiaLanzarUserProfileNotFoundException_cuandoElUsuarioNoTienePerfilFisico() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> logWeightUseCase.execute(userId, 74.8, "cualquier nota"))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("El perfil de usuario no esta creado");

        verify(weightLogRepository, never()).save(any(WeightLog.class));
    }

    @ParameterizedTest(name = "peso invalido = {0}")
    @ValueSource(doubles = {0.0, -1.0, -25.5, 300.5, 400.0})
    @DisplayName("Debe lanzar IllegalArgumentException y no registrar cuando el peso no cumple los limites del VO Weight")
    void deberiaLanzarExcepcionDeNegocio_cuandoElPesoNoCumpleLosLimitesDelVO(double pesoInvalido) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> logWeightUseCase.execute(userId, pesoInvalido, "nota"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El peso no puede ser menor que 0 o mayor que 300");

        // La invariante del dominio corta el flujo antes de tocar la persistencia.
        verify(weightLogRepository, never()).save(any(WeightLog.class));
    }

    @Test
    @DisplayName("Debe aceptar los limites inclusivos del VO Weight (300 kg)")
    void deberiaRegistrarElPesaje_cuandoElPesoEsElLimiteSuperiorValido() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.save(any(WeightLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        WeightLog resultado = logWeightUseCase.execute(userId, 300.0, null);

        // Assert (Then)
        assertThat(resultado.getWeight().value()).isEqualTo(300.0);
        verify(weightLogRepository, times(1)).save(any(WeightLog.class));
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private UserProfile perfilValido(UUID userId) {
        return new UserProfile(userId, new Age(28), new Weight(75.5), new Height(178),
                ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);
    }
}