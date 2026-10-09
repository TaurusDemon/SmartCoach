package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.ActivityLevel;
import com.smartcoach.backend.domain.vo.Age;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.FitnessGoal;
import com.smartcoach.backend.domain.vo.Height;
import com.smartcoach.backend.domain.vo.PasswordHash;
import com.smartcoach.backend.domain.vo.Weight;
import com.smartcoach.backend.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class UserProfileRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    /** Necesario para respetar la FK antes de crear el perfil. */
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("IT-01: Debe guardar un perfil y recuperarlo por userId desde PostgreSQL")
    void shouldSaveAndFindProfileByUserIdInRealPostgres() {
        // GIVEN: usuario persistido + perfil de dominio válido.
        UUID userId = crearUsuarioPersistido();

        UserProfile perfil = new UserProfile(
                userId,
                new Age(30),
                new Weight(82.4),
                new Height(181),
                ActivityLevel.INTENSE,
                FitnessGoal.MUSCLE_GAIN);

        // WHEN
        userProfileRepository.save(perfil);
        Optional<UserProfile> recuperado = userProfileRepository.findByUserId(userId);

        // THEN: se comparan campo a campo (los VOs de dominio no exponen equals()).
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getUserId()).isEqualTo(userId);
        assertThat(recuperado.get().getAge().value()).isEqualTo(30);
        assertThat(recuperado.get().getWeight().value()).isEqualTo(82.4);
        assertThat(recuperado.get().getHeight().valueCm()).isEqualTo(181);
        assertThat(recuperado.get().getActivityLevel()).isEqualTo(ActivityLevel.INTENSE);
        assertThat(recuperado.get().getFitnessGoal()).isEqualTo(FitnessGoal.MUSCLE_GAIN);
    }

    @Test
    @DisplayName("IT-02: Debe actualizar el perfil existente del mismo usuario (relación 1:1)")
    void shouldUpdateExistingProfileInRealPostgres() {
        // GIVEN: un usuario con un perfil inicial.
        UUID userId = crearUsuarioPersistido();
        userProfileRepository.save(new UserProfile(
                userId, new Age(30), new Weight(82.4), new Height(181),
                ActivityLevel.INTENSE, FitnessGoal.MUSCLE_GAIN));

        // WHEN: se guarda un perfil nuevo para el MISMO userId (misma PK).
        userProfileRepository.save(new UserProfile(
                userId, new Age(31), new Weight(80.0), new Height(181),
                ActivityLevel.MODERATE, FitnessGoal.WEIGHT_LOSS));

        // THEN: debe seguir habiendo un único perfil, con los datos nuevos.
        Optional<UserProfile> actualizado = userProfileRepository.findByUserId(userId);
        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getAge().value()).isEqualTo(31);
        assertThat(actualizado.get().getWeight().value()).isEqualTo(80.0);
        assertThat(actualizado.get().getFitnessGoal()).isEqualTo(FitnessGoal.WEIGHT_LOSS);
    }

    @Test
    @DisplayName("IT-03: Debe devolver vacío cuando el usuario no tiene perfil")
    void shouldReturnEmptyWhenProfileDoesNotExist() {
        // GIVEN: usuario persistido pero SIN perfil asociado.
        UUID userId = crearUsuarioPersistido();

        // WHEN
        Optional<UserProfile> recuperado = userProfileRepository.findByUserId(userId);

        // THEN: el puerto devuelve Optional vacío, nunca null.
        assertThat(recuperado).isEmpty();
    }

    /**
     * Helper: crea y persiste un {@link User} real para poder referenciarlo desde
     * el perfil. Devuelve su id para reutilizarlo como userId del perfil.
     */
    private UUID crearUsuarioPersistido() {
        UUID userId = UUID.randomUUID();
        Email email = new Email("profile.it." + userId + "@smartcoach.com");
        userRepository.save(new User(userId, "Usuario Perfil IT", email, new PasswordHash("hash-perfil")));
        return userId;
    }
}
