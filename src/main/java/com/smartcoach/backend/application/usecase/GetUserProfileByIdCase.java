package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserNotFoundException;
import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;

import java.util.UUID;

public class GetUserProfileByIdCase {

    UserProfileRepository userProfileRepository;
    public GetUserProfileByIdCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(UUID userId) {
        if(userProfileRepository.findByUserId(userId).isEmpty()){
            throw new UserProfileNotFoundException(userId);
        }
        return userProfileRepository.findByUserId(userId).get();
    }
}
