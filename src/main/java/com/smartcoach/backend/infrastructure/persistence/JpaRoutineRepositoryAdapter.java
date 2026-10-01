package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.infrastructure.persistence.entity.RoutineEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.RoutineMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataRoutineRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRoutineRepositoryAdapter implements RoutineRepository {

    private final SpringDataRoutineRepository springDataRoutineRepository;
    public JpaRoutineRepositoryAdapter(SpringDataRoutineRepository springDataRoutineRepository) {
        this.springDataRoutineRepository = springDataRoutineRepository;
    }

    @Override
    public Routine save(Routine routine) {
        RoutineEntity routineEntity = RoutineMapper.toEntity(routine);
        return RoutineMapper.toDomain(springDataRoutineRepository.save(routineEntity));
    }

    @Override
    public Optional<Routine> findById(UUID id) {
        Optional<RoutineEntity> routineEntityOptional = springDataRoutineRepository.findById(id);
        return routineEntityOptional.map(RoutineMapper::toDomain);
    }

    @Override
    public List<Routine> findByUserId(UUID userId) {
        List<RoutineEntity> routineEntityList = springDataRoutineRepository.findByUserId(userId);
        return routineEntityList.stream().map(input -> RoutineMapper.toDomain(input)).toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRoutineRepository.deleteById(id);
    }
}
