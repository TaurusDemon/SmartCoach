package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.ForbiddenOperationException;
import com.smartcoach.backend.domain.exception.RoutineNotFoundException;
import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.exception.WeightLogNotFoundException;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.*;

import java.util.List;
import java.util.UUID;

public class UpdateWorkoutRoutineUseCase {

    private final RoutineRepository routineRepository;
    private final UserProfileRepository userProfileRepository;
    private final AiCoachPort aiCoachPort;
    private final WeightLogRepository weightLogRepository;

    public UpdateWorkoutRoutineUseCase(RoutineRepository routineRepository, UserProfileRepository userProfileRepository, AiCoachPort aiCoachPort, WeightLogRepository weightLogRepository) {
        this.routineRepository = routineRepository;
        this.userProfileRepository = userProfileRepository;
        this.aiCoachPort = aiCoachPort;
        this.weightLogRepository = weightLogRepository;
    }



    public Routine execute(UUID userId, UUID routineId, String additionalPreferences){

        Routine routineExistente = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException(routineId));

        if(!routineExistente.getUserId().equals(userId)){
            throw new ForbiddenOperationException();
        }
        if (weightLogRepository.findByUserIdOrderByLogDateDesc(userId).isEmpty()){
            throw new WeightLogNotFoundException(userId);
        }

        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(()-> new UserProfileNotFoundException(userId));
        List<WeightLog> weightLogHistory = weightLogRepository.findLastByUserId(userId, 10);
        Routine routine = aiCoachPort.updateRoutine(routineExistente, userProfile, weightLogHistory, additionalPreferences);

        return routineRepository.save(routine);

    }
}
