package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.vo.Email;
import com.smartcoach.backend.infrastructure.persistence.entity.UserEntity;

public class UserPersistenceMapper {


    //Transformar de Domino a Entidad JPA
    public static UserEntity toEntity (User user){
        return new UserEntity(user.getId(), user.getName(), user.getEmail().value());
    }

    //Transformar de entidad JPA a dominio
    public static User toDomain(UserEntity user){
        return new User(user.getId(), user.getName(), new Email(user.getEmail()));
    }
}
