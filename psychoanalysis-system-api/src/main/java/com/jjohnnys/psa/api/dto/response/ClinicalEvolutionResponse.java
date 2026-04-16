package com.jjohnnys.psa.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicalEvolutionResponse(
        UUID id,
        UUID appointmentId,
        UUID psychologistId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
