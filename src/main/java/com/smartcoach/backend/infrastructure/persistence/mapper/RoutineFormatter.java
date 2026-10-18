package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.WeightLog;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RoutineFormatter {

    public static String formatRoutine(Routine routine) {

        if (routine == null) {
            return "No hay rutina antigua";
        }

        List<Exercise> exercises = routine.getExerciseList();

        return exercises.stream()
                .map(e -> String.format(Locale.ROOT, "%s: Category: %s, Sets: %d, Reps:%d, RestSeconds:%d ",
                        e.getName(),
                        e.getExerciseCategory(),
                        e.getExerciseDetails().sets(),
                        e.getExerciseDetails().reps(),
                        e.getExerciseDetails().restSeconds()))
                .collect(Collectors.joining("\n"));
    }
}
