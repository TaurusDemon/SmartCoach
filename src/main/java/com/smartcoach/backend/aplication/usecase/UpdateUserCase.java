package com.smartcoach.backend.aplication.usecase;

import com.smartcoach.backend.domain.expection.UserAlreadyExistsExpection;
import com.smartcoach.backend.domain.expection.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;

import java.util.Objects;
import java.util.UUID;

public class UpdateUserCase {
    private UserRepository userRepository;

    public UpdateUserCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(UUID id, String name, String rawEmail) {
        if(userRepository.findById(id).isEmpty()){
            throw new UserNotFoundException(id);
        }
        Email email = new Email(rawEmail);
        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsExpection(email.toString());
        }
        User user = new User(id, name, email);
        return userRepository.save(user);
    }
}
