package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.ForbiddenOperationException;
import com.smartcoach.backend.domain.exception.RoutineNotFoundException;
import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.exception.WeightLogNotFoundException;
import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.AiCoachPort;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.WeightLogRepository;
import com.smartcoach.backend.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link UpdateWorkoutRoutineUseCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link RoutineRepository},
 * {@link UserProfileRepository}, {@link WeightLogRepository} y
 * {@link AiCoachPort}); la {@link Routine}, el {@link UserProfile} y los
 * {@link WeightLog} que viajan hasta la IA son objetos REALES del dominio:
 * el test verifica la orquestacion, no un doble.
 */
@ExtendWith(MockitoExtension.class)
public class UpdateWorkoutRoutineUseCaseTest {

    @Mock
    private RoutineRepository routineRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private AiCoachPort aiCoachPort;
    @Mock
    private WeightLogRepository weightLogRepository;

    private UpdateWorkoutRoutineUseCase updateWorkoutRoutineUseCase;

    @BeforeEach
    void setUp(){
        updateWorkoutRoutineUseCase = new UpdateWorkoutRoutineUseCase(routineRepository, userProfileRepository, aiCoachPort, weightLogRepository);
    }

    @Test
    @DisplayName("Debe actualizar y guardar la rutina cuando existen rutina, historial de pesajes y perfil fisico")
    void deberiaActualizarYGuardarLaRutina_cuandoExistenRutinaHistorialYPerfil() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        String preferencias = "Mas ejercicios de pierna";

        Routine rutinaExistente = rutinaReal(userId);
        Routine rutinaModificada = rutinaModificada(userId);
        UserProfile perfil = perfilValido(userId);
        List<WeightLog> historialCompleto = historialDePesos(userId, 2);
        List<WeightLog> ultimos10 = historialDePesos(userId, 2);

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaExistente));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historialCompleto);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(weightLogRepository.findLastByUserId(userId, 10)).thenReturn(ultimos10);
        when(aiCoachPort.updateRoutine(rutinaExistente, perfil, ultimos10, preferencias)).thenReturn(rutinaModificada);
        when(routineRepository.save(rutinaModificada)).thenReturn(rutinaModificada);

        // Act (When)
        Routine resultado = updateWorkoutRoutineUseCase.execute(userId, routineId, preferencias);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado).isSameAs(rutinaModificada);
        assertThat(resultado.getId()).isEqualTo(rutinaModificada.getId());
        assertThat(resultado.getName()).isEqualTo("Rutina Hipertrofia B");
        assertThat(resultado.getRoutineGoal()).isEqualTo(RoutineGoal.STRENGTH);

        verify(aiCoachPort, times(1)).updateRoutine(rutinaExistente, perfil, ultimos10, preferencias);
        verify(routineRepository, times(1)).save(rutinaModificada);
    }

    @Test
    @DisplayName("Debe delegar en la IA la rutina existente, el perfil real y el historial recortado")
    void deberiaDelegarEnAiCoachPortLaRutinaElPerfilYElHistorial_cuandoSeActualiza() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        String preferencias = "Enfocarme en tren superior";

        Routine rutinaExistente = rutinaReal(userId);
        Routine rutinaModificada = rutinaModificada(userId);
        UserProfile perfil = perfilValido(userId);
        List<WeightLog> historial = historialDePesos(userId, 3);

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaExistente));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historial);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(weightLogRepository.findLastByUserId(userId, 10)).thenReturn(historial);
        when(aiCoachPort.updateRoutine(any(Routine.class), any(UserProfile.class), anyList(), any(String.class)))
                .thenReturn(rutinaModificada);
        when(routineRepository.save(rutinaModificada)).thenReturn(rutinaModificada);

        // Act (When)
        updateWorkoutRoutineUseCase.execute(userId, routineId, preferencias);

        // Assert (Then): la IA recibe entidades REALES del dominio, no mocks.
        ArgumentCaptor<Routine> routineCaptor = ArgumentCaptor.forClass(Routine.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(aiCoachPort, times(1))
                .updateRoutine(routineCaptor.capture(), profileCaptor.capture(), eq(historial), eq(preferencias));

        assertThat(routineCaptor.getValue()).isSameAs(rutinaExistente);
        assertThat(profileCaptor.getValue()).isSameAs(perfil);

        // El historial que recibe la IA es el recortado (ultimos 10), no el completo.
        verify(weightLogRepository, times(1)).findLastByUserId(userId, 10);
    }

    @Test
    @DisplayName("Debe lanzar RoutineNotFoundException y no continuar cuando la rutina no existe")
    void deberiaLanzarRoutineNotFoundException_cuandoLaRutinaNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        when(routineRepository.findById(routineId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateWorkoutRoutineUseCase.execute(userId, routineId, "preferencias"))
                .isInstanceOf(RoutineNotFoundException.class)
                .hasMessageContaining("No existe rutina con el id");

        // Invariante: se corta en el primer guard; no se toca historial, perfil, IA ni persistencia.
        verify(weightLogRepository, never()).findByUserIdOrderByLogDateDesc(any());
        verify(userProfileRepository, never()).findByUserId(any());
        verify(aiCoachPort, never()).updateRoutine(any(), any(), any(), any());
        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe lanzar ForbiddenOperationException y no actualizar cuando la rutina pertenece a otro usuario")
    void deberiaLanzarForbiddenOperationException_cuandoLaRutinaPerteneceAOtroUsuario() {
        // Arrange (Given): la rutina existe pero su userId no coincide con el del solicitante.
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine rutinaDeOtroUsuario = rutinaReal(UUID.randomUUID());

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaDeOtroUsuario));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateWorkoutRoutineUseCase.execute(userId, routineId, "preferencias"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("No tienes permisos para hacer esta operación");

        // Invariante: sin permiso no se toca historial, perfil, IA ni persistencia.
        verify(weightLogRepository, never()).findByUserIdOrderByLogDateDesc(any());
        verify(userProfileRepository, never()).findByUserId(any());
        verify(aiCoachPort, never()).updateRoutine(any(), any(), any(), any());
        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe lanzar WeightLogNotFoundException y no llamar a la IA cuando el usuario no tiene pesajes")
    void deberiaLanzarWeightLogNotFoundException_cuandoElUsuarioNoTieneHistorialDePeso() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaReal(userId)));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(List.of());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateWorkoutRoutineUseCase.execute(userId, routineId, "preferencias"))
                .isInstanceOf(WeightLogNotFoundException.class)
                .hasMessageContaining("no tiene ningún historial de peso");

        // Invariante: sin pesajes no se consulta el perfil ni se invoca a la IA.
        verify(userProfileRepository, never()).findByUserId(any());
        verify(aiCoachPort, never()).updateRoutine(any(), any(), any(), any());
        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException y no llamar a la IA cuando el usuario no tiene perfil")
    void deberiaLanzarUserProfileNotFoundException_cuandoElUsuarioNoTienePerfilFisico() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaReal(userId)));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historialDePesos(userId, 1));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateWorkoutRoutineUseCase.execute(userId, routineId, "preferencias"))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("El perfil de usuario no esta creado");

        // Invariante: sin perfil no se invoca a la IA ni se escribe en el repositorio.
        verify(aiCoachPort, never()).updateRoutine(any(), any(), any(), any());
        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe propagar el fallo de la IA y no persistir la rutina cuando el adaptador externo falla")
    void deberiaPropagarElFalloDeLaIa_yNoPersistirLaRutina_cuandoElAdaptadorExternoFalla() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine rutinaExistente = rutinaReal(userId);
        UserProfile perfil = perfilValido(userId);
        List<WeightLog> historial = historialDePesos(userId, 2);

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaExistente));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historial);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(weightLogRepository.findLastByUserId(userId, 10)).thenReturn(historial);
        when(aiCoachPort.updateRoutine(rutinaExistente, perfil, historial, "intenso"))
                .thenThrow(new IllegalStateException("El proveedor de IA no esta disponible"));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> updateWorkoutRoutineUseCase.execute(userId, routineId, "intenso"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El proveedor de IA no esta disponible");

        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe actualizar la rutina cuando las preferencias adicionales son nulas")
    void deberiaActualizarLaRutina_cuandoLasPreferenciasAdicionalesSonNulas() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine rutinaExistente = rutinaReal(userId);
        Routine rutinaModificada = rutinaModificada(userId);
        UserProfile perfil = perfilValido(userId);
        List<WeightLog> historial = historialDePesos(userId, 2);

        when(routineRepository.findById(routineId)).thenReturn(Optional.of(rutinaExistente));
        when(weightLogRepository.findByUserIdOrderByLogDateDesc(userId)).thenReturn(historial);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(weightLogRepository.findLastByUserId(userId, 10)).thenReturn(historial);
        when(aiCoachPort.updateRoutine(rutinaExistente, perfil, historial, null)).thenReturn(rutinaModificada);
        when(routineRepository.save(rutinaModificada)).thenReturn(rutinaModificada);

        // Act (When)
        Routine resultado = updateWorkoutRoutineUseCase.execute(userId, routineId, null);

        // Assert (Then)
        assertThat(resultado).isSameAs(rutinaModificada);

        verify(aiCoachPort, times(1)).updateRoutine(rutinaExistente, perfil, historial, null);
        verify(routineRepository, times(1)).save(rutinaModificada);
    }

    private Routine rutinaModificada(UUID userId) {
        List<Exercise> ejercicios = List.of(
                new Exercise(UUID.randomUUID(), "Dominadas", ExerciseCategory.BACK,
                        new ExerciseDetails(4, 8, 90)));
        return new Routine(UUID.randomUUID(), userId, "Rutina Hipertrofia B", RoutineGoal.STRENGTH,
                ejercicios, LocalDateTime.now());
    }

    private List<WeightLog> historialDePesos(UUID userId, int cantidad) {
        List<WeightLog> pesajes = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            pesajes.add(new WeightLog(UUID.randomUUID(), userId, new Weight(75.0 + i),
                    LocalDateTime.now().minusDays(i), "Pesaje " + i));
        }
        return pesajes;
    }

    private UserProfile perfilValido(UUID userId) {
        return new UserProfile(userId, new Age(28), new Weight(75.5), new Height(178),
                ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);
    }

    private Routine rutinaReal(UUID userId) {
        List<Exercise> ejercicios = List.of(
                new Exercise(UUID.randomUUID(), "Press de banca", ExerciseCategory.CHEST,
                        new ExerciseDetails(4, 10, 90)),
                new Exercise(UUID.randomUUID(), "Sentadilla trasera", ExerciseCategory.LEGS,
                        new ExerciseDetails(4, 8, 120)));
        return new Routine(UUID.randomUUID(), userId, "Rutina Hipertrofia A", RoutineGoal.HYPERTROPHY,
                ejercicios, LocalDateTime.now());
    }
}
