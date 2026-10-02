package com.smartcoach.backend.application.usecase;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.port.RoutineRepository;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.domain.vo.ExerciseCategory;
import com.smartcoach.backend.domain.vo.RoutineGoal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreateRoutineUseCase {

    private final RoutineRepository routineRepository;
    public CreateRoutineUseCase(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }

    public Routine execute(UUID userId, String name, RoutineGoal routineGoal, List<ExerciseInput> exerciseInput){

        List<Exercise> exerciseList = exerciseInput.stream().map(input -> new Exercise(UUID.randomUUID(),input.name(),input.exerciseCategory(),new ExerciseDetails(input.sets(), input.reps(), input.restSeconds()))).toList();

        Routine routine = new Routine(UUID.randomUUID(), userId, name, routineGoal,exerciseList,LocalDateTime.now());

        return routineRepository.save(routine);
    }

    public record ExerciseInput(String name, ExerciseCategory exerciseCategory, int sets, int reps, int restSeconds){}
}
