package com.jjohnnys.psa.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID patientId,
        @NotNull UUID psychologistId,
        @NotNull @Future LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        @NotNull @DecimalMin(value = "0.00") BigDecimal appliedValue
) {
}
