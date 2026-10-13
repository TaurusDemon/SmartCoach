package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.ForbiddenOperationException;
import com.smartcoach.backend.domain.exception.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.PasswordEncoderPort;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.domain.vo.Password;
import com.smartcoach.backend.domain.vo.PasswordHash;
import com.smartcoach.backend.domain.vo.Role;

import java.util.Objects;
import java.util.UUID;

public class UpdateUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public UpdateUserCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoderPort = Objects.requireNonNull(passwordEncoder);
    }

    public User execute(UUID id, UUID targetId, String role, String name, String rawEmail, String rawPassword) {

        boolean isOwner = id.equals(targetId);

        if(!role.equals(Role.ADMIN.name()) && !isOwner){
            throw new ForbiddenOperationException();
        }

        if(userRepository.findById(id).isEmpty()){
            throw new UserNotFoundException(id);
        }
        Email email = new Email(rawEmail);
        String encodedPassword = passwordEncoderPort.encode(rawPassword);
        new Password(rawPassword);

        userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new UserAlreadyExistsExpection(email.toString()); });

        User user = new User(id, name, email,new PasswordHash(encodedPassword));
        return userRepository.save(user);
    }
}
