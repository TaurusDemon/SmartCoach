package com.smartcoach.backend.domain.port;

import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;

public interface AiCoachPort {
    Routine generateRoutine(UserProfile userProfile, String additionalPreferences);
}
