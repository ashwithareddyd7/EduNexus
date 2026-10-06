package com.edunexus.backend.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 10-point scale applied to the percentage marks/maxMarks. Pass mark is 40%. */
public final class GradeScale {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private GradeScale() {}

    public static int pointsFor(BigDecimal marks, int maxMarks) {
        if (maxMarks <= 0) return 0;
        BigDecimal scaled = marks.multiply(HUNDRED);
        if (atLeast(scaled, 90, maxMarks)) return 10;
        if (atLeast(scaled, 80, maxMarks)) return 9;
        if (atLeast(scaled, 70, maxMarks)) return 8;
        if (atLeast(scaled, 60, maxMarks)) return 7;
        if (atLeast(scaled, 50, maxMarks)) return 6;
        if (atLeast(scaled, 45, maxMarks)) return 5;
        if (atLeast(scaled, 40, maxMarks)) return 4;
        return 0;
    }

    public static String letterFor(BigDecimal marks, int maxMarks) {
        return switch (pointsFor(marks, maxMarks)) {
            case 10 -> "O";
            case 9 -> "A+";
            case 8 -> "A";
            case 7 -> "B+";
            case 6 -> "B";
            case 5 -> "C";
            case 4 -> "P";
            default -> "F";
        };
    }

    public static boolean isPass(BigDecimal marks, int maxMarks) {
        return pointsFor(marks, maxMarks) >= 4;
    }

    /** For display only; grading itself compares exact values, never this rounded one. */
    public static BigDecimal percentage(BigDecimal marks, int maxMarks) {
        if (maxMarks <= 0) return BigDecimal.ZERO.setScale(2);
        return marks.multiply(HUNDRED)
                .divide(BigDecimal.valueOf(maxMarks), 2, RoundingMode.HALF_UP);
    }

    private static boolean atLeast(BigDecimal scaledMarks, int percent, int maxMarks) {
        return scaledMarks.compareTo(BigDecimal.valueOf((long) percent * maxMarks)) >= 0;
    }
}