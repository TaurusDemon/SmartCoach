package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;

import java.util.UUID;

public class GetUserByIdCase {
    private final UserRepository userRepository;

    public GetUserByIdCase(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User execute(UUID id){
        return userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(id));
    }
}
