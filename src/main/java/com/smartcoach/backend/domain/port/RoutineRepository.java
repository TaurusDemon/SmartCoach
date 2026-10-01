package com.smartcoach.backend.domain.port;

import com.smartcoach.backend.domain.model.Routine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepository {
    Routine save(Routine routine);
    Optional<Routine> findById(UUID id);
    List<Routine> findByUserId(UUID userId);
    void deleteById(UUID id);
}
