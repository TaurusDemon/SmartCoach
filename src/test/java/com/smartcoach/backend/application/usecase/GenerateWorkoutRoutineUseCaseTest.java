package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.AiCoachPort;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.vo.ActivityLevel;
import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.FitnessGoal;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.RoutineGoal;
import com.smartcoach.backend.domain.vo.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link GenerateWorkoutRoutineUseCase}.
 *
 * Se mockea unicamente el puerto de salida {@link AiCoachPort} (adaptador externo)
 * y los repositorios. La {@link Routine} devuelta por la IA y el {@link UserProfile}
 * son objetos REALES del dominio: el test verifica la orquestacion, no un doble.
 */
@ExtendWith(MockitoExtension.class)
class GenerateWorkoutRoutineUseCaseTest {

    @Mock
    private AiCoachPort aiCoachPort;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoutineRepository routineRepository;

    private GenerateWorkoutRoutineUseCase generateWorkoutRoutineUseCase;

    @BeforeEach
    void setUp() {
        generateWorkoutRoutineUseCase = new GenerateWorkoutRoutineUseCase(aiCoachPort, userProfileRepository, routineRepository);
    }

    @Test
    @DisplayName("Debe generar y persistir la rutina cuando el usuario tiene perfil fisico configurado")
    void deberiaGenerarYGuardarRutina_cuandoPerfilExisteYEntradasSonValidas() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfil = perfilValido(userId);
        Routine rutinaGeneradaPorIa = rutinaReal(userId);
        String preferencias = "Prefiero ejercicios de Tren Superior";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(aiCoachPort.generateRoutine(perfil, preferencias)).thenReturn(rutinaGeneradaPorIa);
        when(routineRepository.save(rutinaGeneradaPorIa)).thenReturn(rutinaGeneradaPorIa);

        // Act (When)
        Routine resultado = generateWorkoutRoutineUseCase.execute(userId, preferencias);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(rutinaGeneradaPorIa.getId());
        assertThat(resultado.getUserId()).isEqualTo(userId);
        assertThat(resultado.getRoutineGoal()).isEqualTo(RoutineGoal.HYPERTROPHY);
        assertThat(resultado.getExerciseList()).hasSize(2);

        // Una sola consulta: el SUT resuelve el perfil con Optional.orElseThrow (sin doble findByUserId).
        verify(userProfileRepository, times(1)).findByUserId(userId);
        verify(aiCoachPort, times(1)).generateRoutine(perfil, preferencias);
        verify(routineRepository, times(1)).save(rutinaGeneradaPorIa);
    }

    @Test
    @DisplayName("Debe delegar en AiCoachPort el perfil real del usuario y las preferencias adicionales")
    void deberiaDelegarEnAiCoachPortElPerfilReal_cuandoElUsuarioTienePerfilConfigurado() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfil = perfilValido(userId);
        Routine rutinaGeneradaPorIa = rutinaReal(userId);
        String preferencias = "Solo ejercicios de cuerpo entero";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(aiCoachPort.generateRoutine(any(UserProfile.class), any(String.class))).thenReturn(rutinaGeneradaPorIa);
        when(routineRepository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        Routine resultado = generateWorkoutRoutineUseCase.execute(userId, preferencias);

        // Assert (Then): la IA recibe la entidad de dominio real, no un mock.
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(aiCoachPort, times(1))
                .generateRoutine(profileCaptor.capture(), eq(preferencias));

        UserProfile perfilEntregado = profileCaptor.getValue();
        assertThat(perfilEntregado).isSameAs(perfil);
        assertThat(perfilEntregado.getUserId()).isEqualTo(userId);
        assertThat(perfilEntregado.getAge().value()).isEqualTo(28);
        assertThat(perfilEntregado.getWeight().value()).isEqualTo(75.5);
        assertThat(perfilEntregado.getHeight().valueCm()).isEqualTo(178);
        assertThat(perfilEntregado.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(perfilEntregado.getFitnessGoal()).isEqualTo(FitnessGoal.MUSCLE_GAIN);

        assertThat(resultado.getName()).isEqualTo(rutinaGeneradaPorIa.getName());
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException y no generar ni persistir rutina cuando el usuario no tiene perfil")
    void deberiaLanzarUserProfileNotFoundException_cuandoElUsuarioNoTienePerfilFisico() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> generateWorkoutRoutineUseCase.execute(userId, "cualquier preferencia"))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("El perfil de usuario no esta creado");

        // Invariante: sin perfil no se invoca a la IA ni se escribe en el repositorio.
        verify(aiCoachPort, never()).generateRoutine(any(UserProfile.class), any(String.class));
        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe propagar el fallo de la IA y no persistir la rutina cuando el adaptador externo falla")
    void deberiaPropagarElFalloDeLaIa_yNoPersistirLaRutina_cuandoElAdaptadorExternoFalla() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfil = perfilValido(userId);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(aiCoachPort.generateRoutine(perfil, "hipertofia"))
                .thenThrow(new IllegalStateException("El proveedor de IA no esta disponible"));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> generateWorkoutRoutineUseCase.execute(userId, "hipertofia"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El proveedor de IA no esta disponible");

        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe aceptar preferencias vacias cuando el usuario tiene perfil configurado")
    void deberiaGenerarRutina_cuandoLasPreferenciasAdicionalesSonVacias() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfil = perfilValido(userId);
        Routine rutinaGeneradaPorIa = rutinaReal(userId);

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfil));
        when(aiCoachPort.generateRoutine(perfil, null)).thenReturn(rutinaGeneradaPorIa);
        when(routineRepository.save(rutinaGeneradaPorIa)).thenReturn(rutinaGeneradaPorIa);

        // Act (When)
        Routine resultado = generateWorkoutRoutineUseCase.execute(userId, null);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        verify(aiCoachPort, times(1)).generateRoutine(perfil, null);
        verify(routineRepository, times(1)).save(rutinaGeneradaPorIa);
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