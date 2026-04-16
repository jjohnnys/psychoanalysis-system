package com.jjohnnys.psa.application.usecase.command;

import com.jjohnnys.psa.domain.Specialty;

import java.math.BigDecimal;

public record RegisterPsychologistCommand(
        String name,
        String crp,
        Specialty specialty,
        BigDecimal baseSessionRate
) {
}
