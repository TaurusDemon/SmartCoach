package com.smartcoach.backend.domain.port;

import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.model.WeightLog;

import java.util.List;
import java.util.UUID;

public interface AiCoachPort {
    Routine generateRoutine(UserProfile userProfile, String additionalPreferences);
    Routine updateRoutine (Routine routine, UserProfile userProfile, List<WeightLog> weightLogHistory, String additionalPreferences);
}
