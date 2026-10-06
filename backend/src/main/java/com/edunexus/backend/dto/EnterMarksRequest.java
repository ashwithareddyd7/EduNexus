package com.edunexus.backend.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record EnterMarksRequest(
        @NotNull Long studentId,
        @NotNull Long subjectId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal marks) {}