package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.port.UserRepository;

import java.util.List;
import java.util.UUID;

public class GetRoutinesByUserIdCase {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    public GetRoutinesByUserIdCase(RoutineRepository routineRepository, UserRepository userRepository) {
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
    }

    public List<Routine> execute(UUID uuid) {

        if(userRepository.findById(uuid).isEmpty())
            throw new UserNotFoundException(uuid);

        return routineRepository.findByUserId(uuid);
    }
}
