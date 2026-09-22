package com.smartcoach.backend.infrastructure.persistence.repository;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email); //Spring genera el SELECT * FROM users WHERE email = ?
    Optional<UserEntity> findById(UUID id);
    void deleteById(UUID id);
}
