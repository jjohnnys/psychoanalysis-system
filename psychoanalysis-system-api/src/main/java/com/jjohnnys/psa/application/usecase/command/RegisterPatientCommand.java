package com.jjohnnys.psa.application.usecase.command;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterPatientCommand(
        String name,
        String cpf,
        LocalDate birthDate,
        String email,
        String phone,
        UUID guardianId
) {
}
