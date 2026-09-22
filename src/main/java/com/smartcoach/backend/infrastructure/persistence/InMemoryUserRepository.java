package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.User;
import com.smartcoach.backend.domain.port.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//public class InMemoryUserRepository implements UserRepository {
//    private final Map<UUID, User> database = new HashMap<>();
//
//    @Override
//    public User save(User user) {
//        database.put(user.getId(), user);
//        System.out.println("💾 [BD Memoria] Usuario guardado con éxito: " + user.getName() + " (" + user.getEmail().value() + ")");
//        return user;
//    }
//}
