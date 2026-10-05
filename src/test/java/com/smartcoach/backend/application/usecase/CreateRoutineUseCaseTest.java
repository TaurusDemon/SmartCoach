package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.application.usecase.CreateRoutineUseCase.ExerciseInput;
import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.RoutineGoal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
 * {@link CreateRoutineUseCase}.
 *
 * Solo se mockea el puerto de salida {@link RoutineRepository}; el
 * {@link ExerciseInput} de entrada y los {@link Exercise}/{@link Routine}
 * generados son tipos REALES del dominio.
 */
@ExtendWith(MockitoExtension.class)
class CreateRoutineUseCaseTest {

    @Mock
    private RoutineRepository routineRepository;

    private CreateRoutineUseCase createRoutineUseCase;

    @BeforeEach
    void setUp() {
        createRoutineUseCase = new CreateRoutineUseCase(routineRepository);
    }

    @Test
    @DisplayName("Debe crear y guardar la rutina mapeando los ejercicios de entrada")
    void deberiaCrearYGuardarLaRutina_cuandoLosEjerciciosDeEntradaSonValidos() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        LocalDateTime antes = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        List<ExerciseInput> inputs = List.of(
                new ExerciseInput("Press de banca", ExerciseCategory.CHEST, 4, 10, 90),
                new ExerciseInput("Sentadilla trasera", ExerciseCategory.LEGS, 4, 8, 120));

        when(routineRepository.save(any(Routine.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        Routine resultado = createRoutineUseCase.execute(
                userId, "Rutina Hipertrofia", RoutineGoal.HYPERTROPHY, inputs);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getUserId()).isEqualTo(userId);
        assertThat(resultado.getName()).isEqualTo("Rutina Hipertrofia");
        assertThat(resultado.getRoutineGoal()).isEqualTo(RoutineGoal.HYPERTROPHY);
        assertThat(resultado.getExerciseList()).hasSize(2);
        assertThat(resultado.getCreatedAt())
                .isAfterOrEqualTo(antes)
                .isBeforeOrEqualTo(LocalDateTime.now());

        verify(routineRepository, times(1)).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe transformar cada ExerciseInput en un Exercise con sus detalles de dominio")
    void deberiaMapearCadaEntradaEnUnEjercicio_cuandoLaRutinaEsValida() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        List<ExerciseInput> inputs = List.of(
                new ExerciseInput("Press de banca", ExerciseCategory.CHEST, 4, 10, 90),
                new ExerciseInput("Sentadilla trasera", ExerciseCategory.LEGS, 5, 8, 120));

        when(routineRepository.save(any(Routine.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        createRoutineUseCase.execute(userId, "Rutina Full Body", RoutineGoal.STRENGTH, inputs);

        // Assert (Then): capturamos la entidad real enviada al puerto de salida.
        ArgumentCaptor<Routine> routineCaptor = ArgumentCaptor.forClass(Routine.class);
        verify(routineRepository, times(1)).save(routineCaptor.capture());

        List<Exercise> ejercicios = routineCaptor.getValue().getExerciseList();
        assertThat(ejercicios).hasSize(2);
        assertThat(ejercicios).extracting(Exercise::getName)
                .containsExactly("Press de banca", "Sentadilla trasera");
        assertThat(ejercicios).extracting(Exercise::getExerciseCategory)
                .containsExactly(ExerciseCategory.CHEST, ExerciseCategory.LEGS);
        assertThat(ejercicios).extracting(Exercise::getExerciseDetails)
                .containsExactly(new ExerciseDetails(4, 10, 90), new ExerciseDetails(5, 8, 120));
        assertThat(ejercicios).extracting(Exercise::getId).doesNotContainNull();
        assertThat(ejercicios).extracting(Exercise::getId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar cuando la lista de ejercicios esta vacia")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaListaDeEjerciciosEstaVacia() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createRoutineUseCase.execute(
                userId, "Rutina Vacia", RoutineGoal.HYPERTROPHY, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La rutina debe contener al menos un ejercicio");

        verify(routineRepository, never()).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar cuando la rutina supera los 15 ejercicios")
    void deberiaLanzarExcepcionDeNegocio_cuandoLaRutinaSuperaElMaximoDeEjercicios() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        List<ExerciseInput> demasiados = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            demasiados.add(new ExerciseInput("Ejercicio " + i, ExerciseCategory.CORE, 3, 12, 45));
        }

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createRoutineUseCase.execute(
                userId, "Rutina Excesiva", RoutineGoal.HYPERTROPHY, demasiados))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Una rutina no puede tener más de 15 ejercicios");

        verify(routineRepository, never()).save(any(Routine.class));
    }

    @ParameterizedTest(name = "series={0}, repeticiones={1}, descanso={2}")
    @CsvSource({"0, 10, 90", "4, 0, 90", "4, 10, 0", "-1, 10, 90"})
    @DisplayName("Debe lanzar IllegalArgumentException y no guardar cuando los detalles del ejercicio son invalidos")
    void deberiaLanzarExcepcionDeNegocio_cuandoLosDetallesDelEjercicioSonInvalidos(
            int series, int repeticiones, int descanso) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        List<ExerciseInput> inputs = List.of(
                new ExerciseInput("Ejercicio invalido", ExerciseCategory.CHEST, series, repeticiones, descanso));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createRoutineUseCase.execute(
                userId, "Rutina Con Ejercicio Invalido", RoutineGoal.HYPERTROPHY, inputs))
                .isInstanceOf(IllegalArgumentException.class);

        verify(routineRepository, never()).save(any(Routine.class));
    }

    @ParameterizedTest(name = "objetivo = {0}")
    @ValueSource(strings = {"HYPERTROPHY", "STRENGTH", "ENDURANCE", "FAT_LOSS"})
    @DisplayName("Debe persistir la rutina con el objetivo solicitado para cada valor del enum RoutineGoal")
    void deberiaPersistirLaRutinaConElObjetivoSolicitado_cuandoElObjetivoEsValido(String objetivo) {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        RoutineGoal routineGoal = RoutineGoal.valueOf(objetivo);
        List<ExerciseInput> inputs = List.of(
                new ExerciseInput("Ejercicio", ExerciseCategory.BACK, 3, 12, 60));

        when(routineRepository.save(any(Routine.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        Routine resultado = createRoutineUseCase.execute(userId, "Rutina " + objetivo, routineGoal, inputs);

        // Assert (Then)
        assertThat(resultado.getRoutineGoal()).isEqualTo(routineGoal);
        verify(routineRepository, times(1)).save(any(Routine.class));
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException y no guardar cuando el nombre de la rutina es nulo")
    void deberiaLanzarExcepcionDeNegocio_cuandoElNombreDeLaRutinaEsNulo() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        List<ExerciseInput> inputs = List.of(
                new ExerciseInput("Ejercicio", ExerciseCategory.CORE, 3, 15, 30));

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> createRoutineUseCase.execute(userId, null, RoutineGoal.ENDURANCE, inputs))
                .isInstanceOf(NullPointerException.class);

        verify(routineRepository, never()).save(any(Routine.class));
    }
}