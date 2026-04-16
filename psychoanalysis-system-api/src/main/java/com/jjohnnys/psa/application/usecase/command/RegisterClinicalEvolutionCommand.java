package com.jjohnnys.psa.application.usecase.command;

import java.util.UUID;

public record RegisterClinicalEvolutionCommand(
        UUID appointmentId,
        UUID psychologistId,
        String content
) {
}
