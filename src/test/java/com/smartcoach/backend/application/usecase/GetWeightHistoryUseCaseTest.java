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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link GetWeightLogHistoryUseCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link UserProfileRepository} y
 * {@link WeightLogRepository}); los registros de {@link WeightLog} son objetos
 * REALES del dominio construidos con fechas deterministas.
 */
@ExtendWith(MockitoExtension.class)
class GetWeightHistoryUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private WeightLogRepository weightLogRepository;

    private GetWeightLogHistoryUseCase getWeightHistoryUseCase;

    @BeforeEach
    void setUp() {
        getWeightHistoryUseCase = new GetWeightLogHistoryUseCase(weightLogRepository, userProfileRepository);
    }

    @Test
    @DisplayName("Debe retornar la lista de pesajes del usuario ordenada por fecha descendente")
    void deberiaRetornarLaListaOrdenadaDePesajes_cuandoElUsuarioTieneRegistrosPrevios() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        LocalDateTime reciente = LocalDateTime.of(2026, 3, 10, 8, 0);
        LocalDateTime medio = LocalDateTime.of(2026, 3, 5, 8, 0);
        LocalDateTime antiguo = LocalDateTime.of(2026, 2, 28, 8, 0);

        // El puerto devuelve la coleccion ya ordenada de forma descendente.
        List<WeightLog> historial = List.of(
                new WeightLog(UUID.randomUUID(), userId, new Weight(74.8), reciente, "Ultimo peso"),
                new WeightLog(UUID.randomUUID(), userId, new Weight(75.5), medio, null),
                new WeightLog(UUID.randomUUID(), userId, new Weight(76.3), antiguo, "Inicio del registro"));

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historial);

        // Act (When)
        List<WeightLog> resultado = getWeightHistoryUseCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull().hasSize(3);
        assertThat(resultado).extracting(WeightLog::getWeight)
                .containsExactly(new Weight(74.8), new Weight(75.5), new Weight(76.3));
        assertThat(resultado).extracting(WeightLog::getLogDate)
                .containsExactly(reciente, medio, antiguo);
        assertThat(resultado).extracting(WeightLog::getUserId)
                .containsOnly(userId);
        assertThat(resultado.get(0).getNotes()).isEqualTo("Ultimo peso");

        verify(userProfileRepository, times(1)).findByUserId(userId);
        verify(weightLogRepository, times(1)).findByUserIdOrderByLogDateDesc(userId);
    }

    @Test
    @DisplayName("Debe retornar una lista vacia cuando el usuario no tiene pesajes registrados")
    void deberiaRetornarListaVacia_cuandoElUsuarioNoTieneRegistrosPrevios() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(List.of());

        // Act (When)
        List<WeightLog> resultado = getWeightHistoryUseCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull().isEmpty();
        verify(weightLogRepository, times(1)).findByUserIdOrderByLogDateDesc(userId);
    }

    @Test
    @DisplayName("Debe retornar solo los registros del usuario consultado cuando existen varios usuarios")
    void deberiaRetornarSoloLosRegistrosDelUsuarioConsultado_cuandoExistenVariosUsuarios() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID otroUserId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.of(2026, 3, 10, 8, 0);

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(perfilValido(userId)));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId))
                .thenReturn(List.of(new WeightLog(UUID.randomUUID(), userId, new Weight(74.8), fecha, "Mio")));

        // Act (When)
        List<WeightLog> resultado = getWeightHistoryUseCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(userId).isNotEqualTo(otroUserId);
        verify(weightLogRepository, times(1)).findByUserIdOrderByLogDateDesc(userId);
        verify(weightLogRepository, never()).findByUserIdOrderByLogDateDesc(otroUserId);
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException y no consultar pesajes cuando el usuario no tiene perfil")
    void deberiaLanzarUserProfileNotFoundException_cuandoElUsuarioNoTienePerfilFisico() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> getWeightHistoryUseCase.execute(userId))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("El perfil de usuario no esta creado");

        verify(weightLogRepository, never()).findByUserIdOrderByLogDateDesc(userId);
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private UserProfile perfilValido(UUID userId) {
        return new UserProfile(userId, new Age(28), new Weight(75.5), new Height(178),
                ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);
    }
}