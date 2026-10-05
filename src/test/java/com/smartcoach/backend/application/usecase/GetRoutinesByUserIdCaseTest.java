package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.PasswordHash;
import com.smartcoach.backend.domain.vo.RoutineGoal;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link GetRoutinesByUserIdCase}.
 *
 * Se mockean unicamente los puertos de salida ({@link UserRepository} y
 * {@link RoutineRepository}); las rutinas y ejercicios son entidades REALES.
 */
@ExtendWith(MockitoExtension.class)
class GetRoutinesByUserIdCaseTest {

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private UserRepository userRepository;

    private GetRoutinesByUserIdCase getRoutinesByUserIdCase;

    @BeforeEach
    void setUp() {
        getRoutinesByUserIdCase = new GetRoutinesByUserIdCase(routineRepository, userRepository);
    }

    @Test
    @DisplayName("Debe retornar las rutinas del usuario cuando el usuario existe")
    void deberiaRetornarLasRutinasDelUsuario_cuandoElUsuarioExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        List<Routine> rutinas = List.of(rutinaReal(userId, "Rutina Push", RoutineGoal.HYPERTROPHY),
                rutinaReal(userId, "Rutina Pull", RoutineGoal.STRENGTH));

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioReal(userId)));
        when(routineRepository.findByUserId(userId)).thenReturn(rutinas);

        // Act (When)
        List<Routine> resultado = getRoutinesByUserIdCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull().hasSize(2);
        assertThat(resultado).extracting(Routine::getName)
                .containsExactly("Rutina Push", "Rutina Pull");
        assertThat(resultado).extracting(Routine::getUserId)
                .containsOnly(userId);
        assertThat(resultado).extracting(Routine::getRoutineGoal)
                .containsExactly(RoutineGoal.HYPERTROPHY, RoutineGoal.STRENGTH);

        verify(userRepository, times(1)).findById(userId);
        verify(routineRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debe retornar una lista vacia cuando el usuario existe pero no tiene rutinas")
    void deberiaRetornarListaVacia_cuandoElUsuarioExistePeroNoTieneRutinas() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioReal(userId)));
        when(routineRepository.findByUserId(userId)).thenReturn(List.of());

        // Act (When)
        List<Routine> resultado = getRoutinesByUserIdCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull().isEmpty();
        verify(routineRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException y no consultar rutinas cuando el usuario no existe")
    void deberiaLanzarUserNotFoundException_cuandoElUsuarioNoExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> getRoutinesByUserIdCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con el ID: " + userId);

        // Invariante: sin usuario valido no se accede al repositorio de rutinas.
        verify(routineRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("Debe retornar las rutinas sin filtrar ni transformar cuando el usuario tiene varias")
    void deberiaRetornarLasRutinasSinTransformar_cuandoElUsuarioTieneVarias() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        Routine rutina = rutinaReal(userId, "Rutina Legs", RoutineGoal.ENDURANCE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuarioReal(userId)));
        when(routineRepository.findByUserId(userId)).thenReturn(List.of(rutina));

        // Act (When)
        List<Routine> resultado = getRoutinesByUserIdCase.execute(userId);

        // Assert (Then): caso de uso de solo lectura, devuelve la lista del puerto.
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(rutina);
        assertThat(resultado.get(0).getExerciseList()).hasSize(1);
    }

    /**
     * Helpers de arrastre: entidades de dominio REALES (nunca mockeadas).
     */
    private Routine rutinaReal(UUID userId, String name, RoutineGoal goal) {
        List<Exercise> ejercicios = List.of(
                new Exercise(UUID.randomUUID(), "Press de banca", ExerciseCategory.CHEST,
                        new ExerciseDetails(4, 10, 90)));
        return new Routine(UUID.randomUUID(), userId, name, goal, ejercicios, LocalDateTime.now());
    }

    private User usuarioReal(UUID userId) {
        return new User(userId, "Carlos Gomez", new Email("carlos@ejemplo.com"),
                new PasswordHash("contrasena123"));
    }
}