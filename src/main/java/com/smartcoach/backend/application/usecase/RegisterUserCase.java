package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.PasswordEncoderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.Password;
import com.smartcoach.backend.domain.vo.PasswordHash;

import java.util.Objects;
import java.util.UUID;

public class RegisterUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public RegisterUserCase (UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoderPort = Objects.requireNonNull(passwordEncoderPort);
    }

    public User execute (String name, String rawEmail, String rawPassword){

        Email email = new Email(rawEmail);
        String encodedPassword = passwordEncoderPort.encode(rawPassword);
        Password password = new Password(rawPassword);

        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsExpection(email.toString());
        }
        User user = new User(UUID.randomUUID(), name, email, new PasswordHash(encodedPassword));

        return userRepository.save(user);
    }
}
