package com.smartcoach.backend.infrastructure.persistence.mapper;

import com.smartcoach.backend.domain.model.WeightLog;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WeightLogHistoryFormatter {

    public static String format(List<WeightLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return "Sin historial de pesos.";
        }

        List<WeightLog> ordered = logs.stream()
                .sorted(Comparator.comparing(WeightLog::getLogDate))
                .toList();

        String lines = ordered.stream()
                .map(l -> String.format(Locale.ROOT, "%s: %.1f kg",
                        l.getLogDate().toLocalDate(),
                        l.getWeight().value()))
                .collect(Collectors.joining("\n"));

        double first = ordered.get(0).getWeight().value();
        double last = ordered.get(ordered.size() - 1).getWeight().value();

        return String.format(Locale.ROOT,
                "Pesajes (del más antiguo al más reciente):%n%s%n"
                        + "Resumen: peso inicial %.1f kg, peso actual %.1f kg, cambio %+.1f kg.",
                lines, first, last, last - first);
    }
}
