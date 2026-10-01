package com.smartcoach.backend.infrastructure.persistence.repository;

import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.infrastructure.persistence.entity.RoutineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRoutineRepository extends JpaRepository<RoutineEntity, UUID> {
    List<RoutineEntity> findByUserId(UUID userId);
}
