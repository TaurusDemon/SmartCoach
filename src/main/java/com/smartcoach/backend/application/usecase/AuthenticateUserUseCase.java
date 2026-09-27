package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.PasswordEncoderPort;
import com.smartcoach.backend.domain.port.TokenProviderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;

public class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort, TokenProviderPort tokenProviderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    public String execute(String email, String password){

        if(userRepository.findByEmail(new  Email(email)).isEmpty()){
            throw new IllegalArgumentException("El email no existe en el sistema");
        }
        User user = userRepository.findByEmail(new Email(email)).get();

        if(!passwordEncoderPort.matches(password, user.getPassword().value())){
            throw new IllegalArgumentException("Password incorrecto");
        }

        return tokenProviderPort.generateToken(user.getId(), user.getEmail().value());
    }
}
