package com.jjohnnys.psa.api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String name,
        String cpf,
        LocalDate birthDate,
        String email,
        String phone,
        UUID guardianId,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
