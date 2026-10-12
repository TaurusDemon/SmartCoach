package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.RoutineGoal;
import com.smartcoach.backend.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class RoutineRepositoryAdapterITest extends AbstractIntegrationTest {

    @Autowired
    private RoutineRepository routineRepository;

    @Test
    @DisplayName("IT-01: Debe guardar una rutina con sus ejercicios y recuperarla por ID (cascade)")
    void shouldSaveRoutineWithExercisesAndFindByIdInRealPostgres() {
        // GIVEN: agregado de dominio completo (rutina + 2 ejercicios).
        UUID routineId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Routine routine = new Routine(
                routineId,
                userId,
                "Full Body Principiante",
                RoutineGoal.STRENGTH,
                List.of(
                        new Exercise(UUID.randomUUID(), "Sentadilla", ExerciseCategory.LEGS, new ExerciseDetails(4, 10, 90)),
                        new Exercise(UUID.randomUUID(), "Press Banca", ExerciseCategory.CHEST, new ExerciseDetails(3, 12, 60))),
                LocalDateTime.of(2026, 5, 1, 9, 0));

        // WHEN
        routineRepository.save(routine);
        Optional<Routine> recuperada = routineRepository.findById(routineId);

        // THEN: la cabecera y los ejercicios deben volver completos.
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getUserId()).isEqualTo(userId);
        assertThat(recuperada.get().getName()).isEqualTo("Full Body Principiante");
        assertThat(recuperada.get().getRoutineGoal()).isEqualTo(RoutineGoal.STRENGTH);
        assertThat(recuperada.get().getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 9, 0));

        assertThat(recuperada.get().getExerciseList()).hasSize(2);
        assertThat(recuperada.get().getExerciseList())
                .extracting(Exercise::getName)
                .containsExactlyInAnyOrder("Sentadilla", "Press Banca");
        assertThat(recuperada.get().getExerciseList())
                .extracting(e -> e.getExerciseDetails().sets())
                .containsExactlyInAnyOrder(4, 3);
    }

    @Test
    @DisplayName("IT-02: Debe recuperar solo las rutinas del usuario indicado")
    void shouldFindOnlyRoutinesOfGivenUser() {
        // GIVEN: dos usuarios, cada uno con su rutina.
        UUID userId = UUID.randomUUID();
        UUID otroUserId = UUID.randomUUID();

        routineRepository.save(rutinaDe(userId, "Rutina del usuario"));
        routineRepository.save(rutinaDe(otroUserId, "Rutina ajena"));

        // WHEN
        List<Routine> rutinas = routineRepository.findByUserId(userId);

        // THEN
        assertThat(rutinas).hasSize(1);
        assertThat(rutinas.get(0).getUserId()).isEqualTo(userId);
        assertThat(rutinas.get(0).getName()).isEqualTo("Rutina del usuario");
    }

    @Test
    @DisplayName("IT-03: Debe eliminar una rutina y sus ejercicios asociados (cascade delete)")
    void shouldDeleteRoutineAndItsExercisesInRealPostgres() {
        // GIVEN: guardamos y capturamos el id REAL que genera el helper.
        UUID userId = UUID.randomUUID();
        Routine guardada = routineRepository.save(rutinaDe(userId, "Rutina a borrar"));
        UUID routineId = guardada.getId();

        // WHEN
        routineRepository.deleteById(routineId);

        // THEN
        assertThat(routineRepository.findById(routineId)).isEmpty();
        assertThat(routineRepository.findByUserId(userId)).isEmpty();
    }

    /** Helper: crea una rutina válida (al menos un ejercicio) para el usuario dado. */
    private Routine rutinaDe(UUID userId, String nombre) {
        return new Routine(
                UUID.randomUUID(),
                userId,
                nombre,
                RoutineGoal.HYPERTROPHY,
                List.of(new Exercise(UUID.randomUUID(), "Dominadas", ExerciseCategory.BACK, new ExerciseDetails(3, 8, 120))),
                LocalDateTime.now());
    }
}
