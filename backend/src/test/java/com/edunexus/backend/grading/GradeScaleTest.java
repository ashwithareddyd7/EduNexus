package com.edunexus.backend.grading;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GradeScaleTest {

    @ParameterizedTest
    @CsvSource({"100,10,O", "90,10,O", "89.99,9,A+", "80,9,A+", "79.99,8,A", "70,8,A",
                "69.99,7,B+", "60,7,B+", "59.99,6,B", "50,6,B", "49.99,5,C", "45,5,C",
                "44.99,4,P", "40,4,P", "39.99,0,F", "0,0,F"})
    void boundariesOutOf100(String marks, int points, String letter) {
        assertEquals(points, GradeScale.pointsFor(new BigDecimal(marks), 100));
        assertEquals(letter, GradeScale.letterFor(new BigDecimal(marks), 100));
    }

    @Test
    void usesPercentageForOtherMaxMarks() {
        assertEquals(10, GradeScale.pointsFor(new BigDecimal("45"), 50));    // 90%
        assertEquals(9, GradeScale.pointsFor(new BigDecimal("44.5"), 50));   // 89%
        assertEquals(4, GradeScale.pointsFor(new BigDecimal("20"), 50));     // 40%
        assertEquals(0, GradeScale.pointsFor(new BigDecimal("19.99"), 50));  // 39.98%
    }

    @Test
    void passMarkIs40Percent() {
        assertFalse(GradeScale.isPass(new BigDecimal("39.99"), 100));
        assertTrue(GradeScale.isPass(new BigDecimal("40"), 100));
    }

    @Test
    void percentageIsRoundedForDisplay() {
        assertEquals(new BigDecimal("85.00"), GradeScale.percentage(new BigDecimal("85"), 100));
        assertEquals(new BigDecimal("33.33"), GradeScale.percentage(new BigDecimal("10"), 30));
    }
}