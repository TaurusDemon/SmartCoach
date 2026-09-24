package com.smartcoach.backend.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class WeightTest {

    @Test
    @DisplayName("Debe crear Weight correctamente cuando el valor es válido")
    void shouldCreateWeightWhenValueIsValid() {
        double validWeight = 75.5;

        Weight weight = new Weight(validWeight);

        assertEquals(validWeight, weight.value());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -10.0, -0.1, 351.0})
    @DisplayName("Debe lanzar IllegalArgumentException cuando el peso está fuera de rango")
    void shouldThrowExceptionWhenWeightIsInvalid(double invalidWeight) {
        assertThrows(IllegalArgumentException.class, () -> new Weight(invalidWeight));
    }
}