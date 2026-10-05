package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.WeightLogRepository;

import java.util.List;
import java.util.UUID;

public class GetWeightLogHistoryUseCase {

    private final WeightLogRepository weightLogRepository;
    private final UserProfileRepository userProfileRepository;

    public GetWeightLogHistoryUseCase(WeightLogRepository weightLogRepository, UserProfileRepository userProfileRepository) {
        this.weightLogRepository = weightLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<WeightLog> execute (UUID userId){

        if(userProfileRepository.findByUserId(userId).isEmpty()){
            throw new UserProfileNotFoundException(userId);
        }

        return weightLogRepository.findByUserIdOrderByLogDateDesc(userId);
    }
}
