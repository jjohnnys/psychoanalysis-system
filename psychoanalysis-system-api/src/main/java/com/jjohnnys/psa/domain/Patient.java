package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

public final class Patient {

    private final UUID id;
    private final String name;
    private final String cpf;
    private final LocalDate birthDate;
    private final String email;
    private final String phone;
    private final UUID guardianId;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Patient(UUID id, String name, String cpf, LocalDate birthDate, String email,
                    String phone, UUID guardianId, boolean active,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.guardianId = guardianId;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Patient create(String name, String cpf, LocalDate birthDate, String email,
                                 String phone, UUID guardianId) {
        validateName(name);
        validateCpf(cpf);
        validateBirthDate(birthDate);
        validateEmail(email);
        validateMinority(birthDate, guardianId);
        var now = LocalDateTime.now();
        return new Patient(UUID.randomUUID(), name, sanitizeCpf(cpf),
                birthDate, email, phone, guardianId, true, now, now);
    }

    public static Patient reconstitute(UUID id, String name, String cpf, LocalDate birthDate,
                                       String email, String phone, UUID guardianId,
                                       boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Patient(id, name, cpf, birthDate, email, phone, guardianId, active, createdAt, updatedAt);
    }

    public Patient deactivate() {
        if (!active) {
            throw new BusinessException("Patient is already inactive");
        }
        return new Patient(id, name, cpf, birthDate, email, phone, guardianId, false, createdAt, LocalDateTime.now());
    }

    public Patient updateContactInfo(String name, String email, String phone) {
        validateName(name);
        validateEmail(email);
        return new Patient(id, name, cpf, birthDate, email, phone, guardianId, active, createdAt, LocalDateTime.now());
    }

    public Patient assignGuardian(UUID newGuardianId) {
        if (!isMinor()) {
            throw new BusinessException("Guardian assignment is only allowed for patients under 18 years old");
        }
        if (newGuardianId == null) {
            throw new BusinessException("Guardian ID cannot be null");
        }
        return new Patient(id, name, cpf, birthDate, email, phone, newGuardianId, active, createdAt, LocalDateTime.now());
    }

    public boolean isMinor() {
        return Period.between(birthDate, LocalDate.now()).getYears() < 18;
    }

    public int age() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private static String sanitizeCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Patient name is required");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("Invalid email: " + email);
        }
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new BusinessException("Birth date is required");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Birth date cannot be in the future");
        }
    }

    private static void validateMinority(LocalDate birthDate, UUID guardianId) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18 && guardianId == null) {
            throw new BusinessException("A guardian is required for patients under 18 years old");
        }
    }

    public static void validateCpf(String cpf) {
        if (cpf == null) {
            throw new BusinessException("CPF is required");
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new BusinessException("Invalid CPF: must have 11 digits");
        }
        if (digits.chars().distinct().count() == 1) {
            throw new BusinessException("Invalid CPF: all digits are the same");
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digits.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = (sum * 10) % 11;
        if (firstDigit >= 10) firstDigit = 0;
        if (firstDigit != (digits.charAt(9) - '0')) {
            throw new BusinessException("Invalid CPF: checksum failed");
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (digits.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = (sum * 10) % 11;
        if (secondDigit >= 10) secondDigit = 0;
        if (secondDigit != (digits.charAt(10) - '0')) {
            throw new BusinessException("Invalid CPF: checksum failed");
        }
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String cpf() { return cpf; }
    public LocalDate birthDate() { return birthDate; }
    public String email() { return email; }
    public String phone() { return phone; }
    public UUID guardianId() { return guardianId; }
    public boolean active() { return active; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
