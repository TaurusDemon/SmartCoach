package com.smartcoach.backend.infrastructure.ai;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.AiCoachPort;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.RoutineGoal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class MockAiAdapter implements AiCoachPort {
    @Override
    public Routine generateRoutine(UserProfile userProfile, String additionalPreferences) {
        String routineTitle = "Rutina IA - " + userProfile.getFitnessGoal().name();

        List<Exercise> exerciseList = List.of(new Exercise(
                        UUID.randomUUID(),
                        "Sentadilla Goblet (IA)",
                        ExerciseCategory.LEGS,
                        new ExerciseDetails(4, 10, 90)
                ),
                new Exercise(
                        UUID.randomUUID(),
                        "Press Militar con Mancuernas (IA)",
                        ExerciseCategory.SHOULDERS,
                        new ExerciseDetails(3, 12, 60)
                ),
                new Exercise(
                        UUID.randomUUID(),
                        "Dominadas Asistidas / Jalón al Pecho (IA)",
                        ExerciseCategory.BACK,
                        new ExerciseDetails(4, 8, 90)
                ));

        return new Routine(UUID.randomUUID(), userProfile.getUserId(),routineTitle, RoutineGoal.HYPERTROPHY,exerciseList, LocalDateTime.now());
    }
}
