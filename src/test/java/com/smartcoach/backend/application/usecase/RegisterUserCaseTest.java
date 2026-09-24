package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.aplication.usecase.RegisterUserCase;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserCaseTest {

    private UserRepository userRepository;
    private RegisterUserCase registerUserCase;

    @BeforeEach
    void setUp() {
        // 1. Creación del Mock (aislamos la dependencia)
        userRepository = mock(UserRepository.class);

        // 2. Inyección manual en la SUT (System Under Test)
        registerUserCase = new RegisterUserCase(userRepository);
    }

    @Test
    @DisplayName("Debe registrar un usuario correctamente cuando los datos son válidos")
    void shouldRegisterUserSuccessfully() {
        // Arrange (Preparación)
        String name = "Carlos";
        String email = "carlos@ejemplo.com";
        String password = "contraseña123";

        // Simulamos que el repositorio devuelve la misma entidad que recibe al guardar
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Ejecución)
        User result = registerUserCase.execute(name, email,password);

        // Assert (Verificación con AssertJ)
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Carlos");
        assertThat(result.getEmail().value()).isEqualTo("carlos@ejemplo.com");
        assertThat(result.getPassword().value()).isEqualTo("contraseña123");

        // Capturamos el objeto real enviado al repositorio para verificar la interacción
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getName()).isEqualTo("Carlos");
        assertThat(capturedUser.getEmail().value()).isEqualTo("carlos@ejemplo.com");
        assertThat(capturedUser.getPassword().value()).isEqualTo("contraseña123");
    }

    @Test
    @DisplayName("Debe lanzar excepción y NO guardar en repositorio si el email es inválido")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange
        String name = "Ana";
        String invalidEmail = "email-sin-arroba";
        String password = "contraseña123";

        // Act & Assert
        assertThatThrownBy(() -> registerUserCase.execute(name, invalidEmail,password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email no tiene un formato válido");

        // Verificamos que al fallar la validación, NUNCA se llegó a llamar al repositorio
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción y NO guardar en repositorio si el email es inválido")
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        // Arrange
        String name = "Ana";
        String invalidEmail = "email@arroba.com";
        String password = "hola";

        // Act & Assert
        assertThatThrownBy(() -> registerUserCase.execute(name, invalidEmail,password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La contraseña debe tener al menos 8 caracteres");

        // Verificamos que al fallar la validación, NUNCA se llegó a llamar al repositorio
        verify(userRepository, never()).save(any());
    }
}