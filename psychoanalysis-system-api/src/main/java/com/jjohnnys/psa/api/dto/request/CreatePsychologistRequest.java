package com.jjohnnys.psa.api.dto.request;

import com.jjohnnys.psa.domain.Specialty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreatePsychologistRequest(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "^\\d{2}/\\d{4,6}$")
        String crp,
        @NotNull Specialty specialty,
        @NotNull @DecimalMin(value = "0.00") BigDecimal baseSessionRate
) {
}
