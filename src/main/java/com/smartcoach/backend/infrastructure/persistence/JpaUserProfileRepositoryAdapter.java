package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;
import com.smartcoach.backend.infrastructure.persistence.entity.UserProfileEntity;
import com.smartcoach.backend.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import com.smartcoach.backend.infrastructure.persistence.repository.SpringDataUserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository springDataUserProfileRepository;
    public  JpaUserProfileRepositoryAdapter(SpringDataUserProfileRepository springDataUserProfileRepository) {
        this.springDataUserProfileRepository = springDataUserProfileRepository;
    }
    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity datos = UserProfilePersistenceMapper.toEntity(userProfile);

        // Upsert explicito. El mapper deja el @Id (userId) a null porque con
        // @MapsId Hibernate lo deriva de la relacion con UserEntity, y eso hace
        // que Spring Data considere SIEMPRE la entidad como "nueva" y ejecute un
        // INSERT: al actualizar un perfil existente se violaba la PK.
        // Solucion: si ya existe, recuperamos la entidad GESTIONADA por JPA y le
        // copiamos los campos (Hibernate hara UPDATE); si no existe, insertamos.
        UserProfileEntity entidadAGuardar = springDataUserProfileRepository.findById(userProfile.getUserId())
                .map(existente -> {
                    existente.setAge(datos.getAge());
                    existente.setWeight(datos.getWeight());
                    existente.setHeight(datos.getHeight());
                    existente.setActivityLevel(datos.getActivityLevel());
                    existente.setFitnessGoal(datos.getFitnessGoal());
                    return existente;
                })
                .orElse(datos);

        UserProfileEntity userProfileEntitySaved = springDataUserProfileRepository.save(entidadAGuardar);

        return UserProfilePersistenceMapper.toDomain(userProfileEntitySaved);
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        Optional<UserProfileEntity> userProfileEntity = springDataUserProfileRepository.findByUserId(userId);
        return userProfileEntity.map(UserProfilePersistenceMapper::toDomain);
    }
}
