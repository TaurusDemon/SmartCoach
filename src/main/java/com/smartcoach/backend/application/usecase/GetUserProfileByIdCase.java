package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.exception.UserProfileNotFoundException;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.UserProfileRepository;

import java.util.UUID;

public class GetUserProfileByIdCase {

    private final UserProfileRepository userProfileRepository;
    public GetUserProfileByIdCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(UUID userId) {
        return userProfileRepository.findByUserId(userId).orElseThrow(()-> new UserProfileNotFoundException(userId));
    }
}
