package com.jjohnnys.psa.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClinicalEvolutionRequest(
        @NotNull UUID appointmentId,
        @NotNull UUID psychologistId,
        @NotBlank String content
) {
}
