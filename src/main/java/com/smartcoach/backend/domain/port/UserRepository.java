package com.smartcoach.backend.domain.port;
import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.vo.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(Email email);
    Optional<User> findById(UUID id);
    void deleteById(UUID id);
}