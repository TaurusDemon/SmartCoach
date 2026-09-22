package com.smartcoach.backend.infrastructure.config;

import com.smartcoach.backend.aplication.usecase.*;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserCase registerUserCase(UserRepository userRepository) {
        return new RegisterUserCase(userRepository);
    }
    @Bean
    public GetUserByIdCase getUserByIdCase(UserRepository userRepository) {
        return new GetUserByIdCase(userRepository);
    }
    @Bean
    public UpdateUserCase updateUserCase(UserRepository userRepository) {
        return new UpdateUserCase(userRepository);
    }

    @Bean
    public DeleteUserCase deleteUserCase(UserRepository userRepository) {
        return new DeleteUserCase(userRepository);
    }

    @Bean
    public CreateOrUpdateUserProfileCase createOrUpdateUserProfileCase(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        return new CreateOrUpdateUserProfileCase(userRepository,userProfileRepository);
    }
}