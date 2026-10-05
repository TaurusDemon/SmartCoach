package com.smartcoach.backend.domain.port;

import com.smartcoach.backend.domain.model.WeightLog;

import java.util.List;
import java.util.UUID;

public interface WeightLogRepository {
    WeightLog save(WeightLog weightLog);
    List<WeightLog> findByUserIdOrderByLogDateDesc(UUID id);
}
