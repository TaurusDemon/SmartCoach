package com.smartcoach.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weight_logs")
public class WeightLogEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private double weight;

    @Column(name= "log_date", nullable = false)
    private LocalDateTime logDate;

    @Column
    private String notes;

    public WeightLogEntity() {}

    public WeightLogEntity(UUID id, UUID userId, double weight, LocalDateTime logDate, String notes) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.logDate = logDate;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public double getWeight() {
        return weight;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public String getNotes() {
        return notes;
    }
}
