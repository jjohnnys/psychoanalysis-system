package com.jjohnnys.psa.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleAppointmentCommand(
        UUID patientId,
        UUID psychologistId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        BigDecimal appliedValue
) {
}
