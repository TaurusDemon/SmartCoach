package com.smartcoach.backend.infrastructure.persistence.repository;

import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.infrastructure.persistence.entity.RoutineEntity;
import com.smartcoach.backend.infrastructure.persistence.entity.WeightLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataWeightLogRepository extends JpaRepository<WeightLogEntity, UUID> {

    List<WeightLogEntity> findByUserIdOrderByLogDateDesc(UUID id);
}
