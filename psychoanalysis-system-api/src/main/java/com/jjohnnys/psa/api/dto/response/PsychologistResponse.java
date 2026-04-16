package com.jjohnnys.psa.api.dto.response;

import com.jjohnnys.psa.domain.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PsychologistResponse(
        UUID id,
        String name,
        String crp,
        Specialty specialty,
        BigDecimal baseSessionRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
