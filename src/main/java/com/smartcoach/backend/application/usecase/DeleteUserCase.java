package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.ForbiddenOperationException;
import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Role;

import java.util.UUID;

public class DeleteUserCase {
    private final UserRepository userRepository;

    public  DeleteUserCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID id, String role, UUID targetId){
        boolean isOwner = id.equals(targetId);

        if(!role.equals(Role.ADMIN.name()) && !isOwner){
            throw new ForbiddenOperationException();
        }

        if(userRepository.findById(id).isEmpty()){
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
