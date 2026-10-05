package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.Weight;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class WeightLog {

    private final UUID id;
    private final UUID userId;
    private final Weight weight;
    private final LocalDateTime logDate;
    private final String notes;

    public  WeightLog(UUID id,UUID userId, Weight weight, LocalDateTime logDate, String notes) {
        this.userId = Objects.requireNonNull(userId, "El ID no puede ser nulo");
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.weight = Objects.requireNonNull(weight,"El peso no puede ser nulo");
        this.logDate = Objects.requireNonNull(logDate);
        this.notes = notes;
    }

    public UUID getUserId() {
        return userId;
    }

    public Weight getWeight() {
        return weight;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public String getNotes() {
        return notes;
    }
}
