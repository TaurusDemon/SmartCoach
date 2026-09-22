package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.infrastructure.persistence.entity.UserEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        //Convertimos la entidad dominio a JPA
        UserEntity userEntitytoSave = UserPersistenceMapper.toEntity(user);
        //Guardamos en BBDD mediante save
        UserEntity userSaved = springDataUserRepository.save(userEntitytoSave);

        //Convertimos de vuelta al modelo de Dominio
        return UserPersistenceMapper.toDomain(userSaved);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        Optional<UserEntity> userEntityOptional = springDataUserRepository.findByEmail(email.value());
        return userEntityOptional.map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        Optional<UserEntity> userEntityOptional = springDataUserRepository.findById(id);
        return userEntityOptional.map(UserPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}
