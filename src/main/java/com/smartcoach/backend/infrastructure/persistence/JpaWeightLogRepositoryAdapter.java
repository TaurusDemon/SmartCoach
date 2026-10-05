package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.WeightLogRepository;
import com.smartcoach.backend.infrastructure.persistence.entity.WeightLogEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.WeightLogMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataWeightLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Component
public class JpaWeightLogRepositoryAdapter implements WeightLogRepository {

    private final SpringDataWeightLogRepository springDataWeightLogRepository;
    public JpaWeightLogRepositoryAdapter(SpringDataWeightLogRepository springDataWeightLogRepository) {
        this.springDataWeightLogRepository = springDataWeightLogRepository;
    }

    @Override
    public WeightLog save(WeightLog weightLog) {

        WeightLogEntity weightLogEntity = springDataWeightLogRepository.save(WeightLogMapper.toEntity(weightLog));

        return WeightLogMapper.toDomain(weightLogEntity);
    }

    @Override
    public List<WeightLog> findByUserIdOrderByLogDateDesc(UUID id) {

        List<WeightLogEntity> weightLogEntityList = springDataWeightLogRepository.findByUserIdOrderByLogDateDesc(id);

        return weightLogEntityList.stream().map(WeightLogMapper :: toDomain).toList();
    }
}
