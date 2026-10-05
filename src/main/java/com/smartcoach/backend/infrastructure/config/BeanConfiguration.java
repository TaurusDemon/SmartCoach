package com.smartcoach.backend.infrastructure.config;

import com.smartcoach.backend.application.usecase.*;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.*;
import com.smartcoach.backend.infrastructure.rest.AuthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserCase registerUserCase(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        return new RegisterUserCase(userRepository, passwordEncoderPort);
    }
    @Bean
    public GetUserByIdCase getUserByIdCase(UserRepository userRepository) {
        return new GetUserByIdCase(userRepository);
    }
    @Bean
    public UpdateUserCase updateUserCase(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        return new UpdateUserCase(userRepository, passwordEncoderPort);
    }

    @Bean
    public DeleteUserCase deleteUserCase(UserRepository userRepository) {
        return new DeleteUserCase(userRepository);
    }

    @Bean
    public CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        return new CreateOrUpdateUserProfileCase(userRepository,userProfileRepository);
    }
    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort, TokenProviderPort tokenProviderPort) {
        return new AuthenticateUserUseCase(userRepository, passwordEncoderPort, tokenProviderPort);
    }

    @Bean
    public CreateRoutineUseCase createRoutineUseCase(RoutineRepository routineRepository) {
        return new CreateRoutineUseCase(routineRepository);
    }

    @Bean
    public GetRoutinesByUserIdCase getRoutinesByUserIdCase(RoutineRepository routineRepository, UserRepository userRepository) {
        return new GetRoutinesByUserIdCase(routineRepository,userRepository);
    }

    @Bean
    public GetUserProfileByIdCase getUserProfileByIdCase(UserProfileRepository userProfileRepository) {
        return new GetUserProfileByIdCase(userProfileRepository);
    }

    @Bean
    public GenerateWorkoutRoutineUseCase generateWorkoutRoutineUseCase(AiCoachPort aiCoachPort, UserProfileRepository userProfileRepository, RoutineRepository routineRepository) {
        return new GenerateWorkoutRoutineUseCase(aiCoachPort, userProfileRepository, routineRepository);
    }

    @Bean
    public CreateWeightLogUseCase createWeightLogUseCase(WeightLogRepository weightLogRepository, UserProfileRepository userProfileRepository) {
        return new CreateWeightLogUseCase(weightLogRepository,userProfileRepository);
    }

    @Bean
    public GetWeightLogHistoryUseCase getWeightLogHistoryUseCase(WeightLogRepository weightLogRepository, UserProfileRepository userProfileRepository) {
        return new GetWeightLogHistoryUseCase(weightLogRepository,userProfileRepository);
    }
}