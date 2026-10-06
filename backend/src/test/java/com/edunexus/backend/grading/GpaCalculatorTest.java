package com.edunexus.backend.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.edunexus.backend.grading.GpaCalculator.Entry;

class GpaCalculatorTest {

    @Test
    void weightedByCredits() {
        // (4*9 + 3*8 + 2*10) / 9 = 80 / 9 = 8.888... -> 8.89
        var result = GpaCalculator.gpa(List.of(new Entry(4, 9), new Entry(3, 8), new Entry(2, 10)));
        assertEquals(new BigDecimal("8.89"), result);
    }

    @Test
    void failedSubjectCountsAsZero() {
        // (4*10 + 4*0) / 8 = 5.00
        var result = GpaCalculator.gpa(List.of(new Entry(4, 10), new Entry(4, 0)));
        assertEquals(new BigDecimal("5.00"), result);
    }

    @Test
    void emptyListIsZero() {
        assertEquals(new BigDecimal("0.00"), GpaCalculator.gpa(List.of()));
    }
}