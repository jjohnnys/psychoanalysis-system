package com.jjohnnys.psa.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientRequest(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$")
        String cpf,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank @Email String email,
        String phone,
        UUID guardianId
) {
}
