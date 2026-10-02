package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.smartcoach.backend.infrastructure.persistence.entity.RoutineEntity;

public class RoutineMapper {

    public static RoutineEntity toEntity(Routine routine){
        RoutineEntity routineEntity = new RoutineEntity(routine.getId(),routine.getUserId(), routine.getName(), routine.getRoutineGoal(),routine.getCreatedAt());

        for(Exercise exercise : routine.getExerciseList()){
            ExerciseEntity exerciseEntity = new ExerciseEntity(
                    exercise.getId(),
                    exercise.getName(),
                    exercise.getExerciseCategory(),
                    exercise.getExerciseDetails().sets(),
                    exercise.getExerciseDetails().reps(),
                    exercise.getExerciseDetails().restSeconds(),
                    routineEntity);
            routineEntity.addExercise(exerciseEntity);
        }

        return routineEntity;
    }

    public static Routine toDomain(RoutineEntity routineEntity){

        return new Routine(routineEntity.getId(),
                routineEntity.getUserId(),
                routineEntity.getName(),
                routineEntity.getRoutineGoal(),
                routineEntity.getExerciseList().stream().map(e -> new Exercise(e.getId(),e.getName(), e.getCategory(), new ExerciseDetails(e.getSets(), e.getReps(), e.getRestSeconds()))).toList(),
                routineEntity.getCreatedAt());
    }
}
