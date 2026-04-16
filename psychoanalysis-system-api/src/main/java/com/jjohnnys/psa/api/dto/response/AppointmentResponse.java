package com.jjohnnys.psa.api.dto.response;

import com.jjohnnys.psa.domain.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID psychologistId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        AppointmentStatus status,
        BigDecimal appliedValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
