package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.ActivityLevel;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.FitnessGoal;
import com.smartcoach.backend.aplication.usecase.CreateOrUpdateUserProfileCase;
import com.smartcoach.backend.domain.vo.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrUpdateUserProfileCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private CreateOrUpdateUserProfileCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrUpdateUserProfileCase(userRepository, userProfileRepository);
    }

    @Test
    @DisplayName("Debe crear o actualizar el perfil cuando el usuario existe")
    void shouldCreateOrUpdateProfileWhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "Carlos Gomez", new Email("carlos@ejemplo.com"),new Password("contraseña123"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfile result = useCase.execute(
                userId,
                28,
                75.5,
                178,
                ActivityLevel.MODERATE,
                FitnessGoal.MUSCLE_GAIN
        );

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(28, result.getAge().value());
        assertEquals(75.5, result.getWeight().value());
        assertEquals(178, result.getHeight().valueCm());
        assertEquals(ActivityLevel.MODERATE, result.getActivityLevel());
        assertEquals(FitnessGoal.MUSCLE_GAIN, result.getFitnessGoal());

        verify(userRepository, times(1)).findById(userId);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                useCase.execute(userId, 28, 75.5, 178, ActivityLevel.MODERATE, FitnessGoal.MUSCLE_GAIN)
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userProfileRepository, never()).save(any());
    }
}