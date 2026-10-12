package com.smartcoach.backend.infrastructure.persistence;

import com.smartcoach.backend.domain.model.WeightLog;
import com.smartcoach.backend.domain.port.WeightLogRepository;
import com.smartcoach.backend.domain.vo.Weight;
import com.smartcoach.backend.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class WeightLogRepositoryAdapterITest extends AbstractIntegrationTest {

    @Autowired
    private WeightLogRepository weightLogRepository;

    @Test
    @DisplayName("IT-01: Deberia guardar y recuperar el historial de pesajes correctamente desde PostgreSQL")
    void shouldSaveAndFindWeightLogsInRealPostgres() {
        //Given
        UUID userId = UUID.randomUUID();
        WeightLog log1 = new WeightLog(UUID.randomUUID(), userId, new Weight(80.5), LocalDateTime.now().minusDays(2), "Primer pesaje");
        WeightLog log2 = new WeightLog(UUID.randomUUID(), userId, new Weight(79.8), LocalDateTime.now(), "Segundo pesaje");

        weightLogRepository.save(log1);
        weightLogRepository.save(log2);

        List<WeightLog> history = weightLogRepository.findByUserIdOrderByLogDateDesc(userId);

        //Asset
        assertThat(history).hasSize(2);

        // Orden DESC: primero el más reciente (log2), después el anterior (log1)
        assertThat(history.get(0).getId()).isEqualTo(log2.getId());
        assertThat(history.get(0).getUserId()).isEqualTo(userId);
        assertThat(history.get(0).getWeight().value()).isEqualTo(79.8);
        assertThat(history.get(0).getNotes()).isEqualTo("Segundo pesaje");

        assertThat(history.get(1).getId()).isEqualTo(log1.getId());
        assertThat(history.get(1).getWeight().value()).isEqualTo(80.5);
        assertThat(history.get(1).getNotes()).isEqualTo("Primer pesaje");
    }

}
