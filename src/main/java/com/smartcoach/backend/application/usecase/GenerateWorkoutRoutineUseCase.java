package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.AiCoachPort;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.port.UserProfileRepository;

import java.util.UUID;

public class GenerateWorkoutRoutineUseCase {

    private final AiCoachPort aiCoachPort;
    private final UserProfileRepository userProfileRepository;
    private final RoutineRepository routineRepository;

    public GenerateWorkoutRoutineUseCase(AiCoachPort aiCoachPort, UserProfileRepository userProfilerepository, RoutineRepository routineRepository) {
        this.aiCoachPort = aiCoachPort;
        this.userProfileRepository = userProfilerepository;
        this.routineRepository = routineRepository;
    }


    public Routine execute(UUID userId, String additionalPreferences){

        Routine routine = aiCoachPort.generateRoutine(userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException(userId)), additionalPreferences);

        return routineRepository.save(routine);
    }
}
