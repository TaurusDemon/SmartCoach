package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.domain.port.WeightLogRepository;
import com.smartcoach.backend.domain.vo.Weight;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreateWeightLogUseCase {

    private final WeightLogRepository weightLogRepository;
    private final UserProfileRepository userProfileRepository;

    public CreateWeightLogUseCase(WeightLogRepository weightLogRepository, UserProfileRepository userProfileRepository) {
        this.weightLogRepository = weightLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public WeightLog execute(UUID userId, double weight, String notes) {

        if(userProfileRepository.findByUserId(userId).isEmpty()){
            throw new UserProfileNotFoundException(userId);
        }

        WeightLog weightLog = new WeightLog(UUID.randomUUID(), userId, new Weight(weight), LocalDateTime.now(), notes);

        return weightLogRepository.save(weightLog);
    }
}
