package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario puro (sin contexto de Spring) del caso de uso
 * {@link GetUserProfileByIdCase}.
 *
 * Solo se mockea el puerto de salida {@link UserProfileRepository}; el
 * {@link UserProfile} se construye como entidad real del dominio.
 */
@ExtendWith(MockitoExtension.class)
class GetUserProfileByIdCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private GetUserProfileByIdCase getUserProfileByIdCase;

    @BeforeEach
    void setUp() {
        getUserProfileByIdCase = new GetUserProfileByIdCase(userProfileRepository);
    }

    @Test
    @DisplayName("Debe retornar el perfil cuando el usuario ya tiene perfil fisico creado")
    void deberiaRetornarElPerfil_cuandoElUsuarioYaTienePerfilCreado() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfilExistente = perfilValido(userId);

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfilExistente));

        // Act (When)
        UserProfile resultado = getUserProfileByIdCase.execute(userId);

        // Assert (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUserId()).isEqualTo(userId);
        assertThat(resultado.getAge().value()).isEqualTo(28);
        assertThat(resultado.getWeight().value()).isEqualTo(75.5);
        assertThat(resultado.getHeight().valueCm()).isEqualTo(178);
        assertThat(resultado.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(resultado.getFitnessGoal()).isEqualTo(FitnessGoal.MUSCLE_GAIN);

        // Una sola consulta: el SUT resuelve con Optional.orElseThrow (sin doble findByUserId).
        verify(userProfileRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debe retornar la misma instancia de perfil que devuelve el repositorio")
    void deberiaRetornarLaMismaInstancia_delRepositorio_cuandoElPerfilExiste() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        UserProfile perfilExistente = perfilValido(userId);

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(perfilExistente));

        // Act (When)
        UserProfile resultado = getUserProfileByIdCase.execute(userId);

        // Assert (Then): el caso de uso solo proyecta, no transforma la entidad.
        assertThat(resultado).isSameAs(perfilExistente);
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException cuando el usuario no tiene perfil creado")
    void deberiaLanzarUserProfileNotFoundException_cuandoElUsuarioNoTienePerfil() {
        // Arrange (Given)
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        assertThatThrownBy(() -> getUserProfileByIdCase.execute(userId))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("El perfil de usuario no esta creado");

        verify(userProfileRepository, times(1)).findByUserId(userId);
    }

    /**
     * Helper de arrastre: entidad de dominio REAL (nunca mockeada).
     */
    private UserProfile perfilValido(UUID userId) {
        return new UserProfile(userId, new Age(28), new Weight(75.5), new Height(178),
                ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN);
    }
}