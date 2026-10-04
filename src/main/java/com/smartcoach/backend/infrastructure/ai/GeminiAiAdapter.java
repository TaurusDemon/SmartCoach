package com.smartcoach.backend.infrastructure.ai;

import com.smartcoach.backend.domain.model.Exercise;
import com.smartcoach.backend.domain.model.Routine;
import com.smartcoach.backend.domain.model.UserProfile;
import com.smartcoach.backend.domain.port.AiCoachPort;
import com.smartcoach.backend.domain.vo.ExerciseDetails;
import com.smartcoach.backend.infrastructure.ai.dto.RoutineAiResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Primary
public class GeminiAiAdapter implements AiCoachPort {

    private final ChatClient chatClient;

    public GeminiAiAdapter(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Routine generateRoutine(UserProfile userProfile, String additionalPreferences) {

        String systemPrompt = """
                Eres un entrenador personal y fisiólogo deportivo de alto nivel. 
                Tu objetivo es diseñar una rutina de entrenamiento altamente efectiva y adaptada
                a las características físicas del usuario.
                
                Debes seleccionar categorías de ejercicio estrictamente válidas dentro de esta lista:
                [CHEST, BACK, LEGS, SHOULDERS, ARMS, BICEPS, TRICEPS, CORE].
                
                Debes seleccionar objetivos de la rutina estrictamente válidas dentro de esta lista:
                [HYPERTROPHY, STRENGTH, ENDURANCE, FAT_LOSS].
                """;

        String userPrompt = String.format("""
                Diseña una rutina de entrenamiento personalizada con la siguiente biometría y contexto:
                - Edad: %d años
                - Peso: %.1f kg
                - Altura: %d cm
                - Nivel de Actividad: %s
                - Objetivo Principal: %s
                - Preferencias Adicionales: %s
                
                Garantiza que la rutina contenga entre 3 y 8 ejercicios coherentes con el objetivo.
                """,
                userProfile.getAge().value(),
                userProfile.getWeight().value(),
                userProfile.getHeight().valueCm(),
                userProfile.getActivityLevel(),
                userProfile.getFitnessGoal(),
                (additionalPreferences!=null && !additionalPreferences.isBlank()) ? additionalPreferences : "Ninguna"
                );

        RoutineAiResponse aiResponse = chatClient.prompt().
                system(systemPrompt).
                user(userPrompt).
                call().
                entity(RoutineAiResponse.class);

        if (aiResponse == null || aiResponse.exercises() == null || aiResponse.exercises().isEmpty()) {
            throw new IllegalStateException("La IA no pudo generar una rutina válida.");
        }

        List<Exercise> exerciseList = aiResponse.exercises().
                stream().
                map(e -> new Exercise(
                        UUID.randomUUID(),
                        e.name(),
                        e.category(),
                        new ExerciseDetails(e.sets(),e.reps(),e.restSeconds())
                )).toList();
        
        Routine response = new Routine(UUID.randomUUID(),userProfile.getUserId(), aiResponse.name(), aiResponse.goal(),exerciseList, LocalDateTime.now());

        return response;
    }
}
