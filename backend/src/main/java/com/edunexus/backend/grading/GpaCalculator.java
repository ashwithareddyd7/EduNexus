package com.edunexus.backend.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class GpaCalculator {
    private GpaCalculator() {}

    public record Entry(int credits, int gradePoints) {}

    /** Used for both SGPA (one semester's entries) and CGPA (all entries). */
    public static BigDecimal gpa(List<Entry> entries) {
        long totalCredits = 0;
        long weighted = 0;
        for (Entry e : entries) {
            totalCredits += e.credits();
            weighted += (long) e.credits() * e.gradePoints();
        }
        if (totalCredits == 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(weighted)
                .divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
    }
}