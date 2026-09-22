package com.smartcoach.backend.aplication.usecase;

import com.smartcoach.backend.domain.expection.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;

import java.util.Objects;
import java.util.UUID;

public class RegisterUserCase {
    private final UserRepository userRepository;

    public RegisterUserCase (UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute (String name, String rawEmail){
        Email email = new Email(rawEmail);
        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsExpection(email.toString());
        }
        User user = new User(UUID.randomUUID(), name, email);

        return userRepository.save(user);
    }
}
