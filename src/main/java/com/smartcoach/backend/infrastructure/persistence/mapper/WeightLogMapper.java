package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.vo.Weight;
import com.smartcoach.backend.infrastructure.persistence.entity.WeightLogEntity;

public class WeightLogMapper {

    public static WeightLogEntity toEntity(WeightLog weightLog) {
        return new WeightLogEntity(weightLog.getId(), weightLog.getUserId(), weightLog.getWeight().value(), weightLog.getLogDate(), weightLog.getNotes());
    }

    public static WeightLog toDomain(WeightLogEntity weightLogEntity) {
        return new WeightLog(weightLogEntity.getId(),weightLogEntity.getUserId(), new Weight(weightLogEntity.getWeight()),weightLogEntity.getLogDate(),weightLogEntity.getNotes());
    }
}
