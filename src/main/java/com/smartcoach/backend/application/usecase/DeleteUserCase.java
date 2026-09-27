package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.port.UserRepository;

import java.util.UUID;

public class DeleteUserCase {
    private UserRepository userRepository;

    public  DeleteUserCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID id){
        if(userRepository.findById(id).isEmpty()){
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
