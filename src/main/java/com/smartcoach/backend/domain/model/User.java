package com.smartcoach.backend.domain.model;

import com.smartcoach.backend.domain.vo.Email;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String name;
    private final Email email;

    public User(UUID id, String name, Email email) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.name = Objects.requireNonNull(name, "El nombre no puede ser nulo");
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Email getEmail() { return email; }
}