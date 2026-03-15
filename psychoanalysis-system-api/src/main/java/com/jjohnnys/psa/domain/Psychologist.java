package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class Psychologist {

    private final UUID id;
    private final String name;
    private final String crp;
    private final Specialty specialty;
    private final BigDecimal baseSessionRate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private static final String CRP_PATTERN = "^\\d{2}/\\d{4,6}$";

    private Psychologist(UUID id, String name, String crp, Specialty specialty,
                         BigDecimal baseSessionRate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.crp = crp;
        this.specialty = specialty;
        this.baseSessionRate = baseSessionRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Psychologist create(String name, String crp, Specialty specialty, BigDecimal baseSessionRate) {
        validateName(name);
        validateCrp(crp);
        validateSpecialty(specialty);
        validateBaseSessionRate(baseSessionRate);
        var now = LocalDateTime.now();
        return new Psychologist(UUID.randomUUID(), name, crp, specialty, baseSessionRate, now, now);
    }

    public static Psychologist reconstitute(UUID id, String name, String crp, Specialty specialty,
                                            BigDecimal baseSessionRate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Psychologist(id, name, crp, specialty, baseSessionRate, createdAt, updatedAt);
    }

    public Psychologist updateBaseSessionRate(BigDecimal newRate) {
        validateBaseSessionRate(newRate);
        return new Psychologist(id, name, crp, specialty, newRate, createdAt, LocalDateTime.now());
    }

    public Psychologist updateName(String newName) {
        validateName(newName);
        return new Psychologist(id, newName, crp, specialty, baseSessionRate, createdAt, LocalDateTime.now());
    }

    public Psychologist updateSpecialty(Specialty newSpecialty) {
        validateSpecialty(newSpecialty);
        return new Psychologist(id, name, crp, newSpecialty, baseSessionRate, createdAt, LocalDateTime.now());
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Psychologist name is required");
        }
    }

    public static void validateCrp(String crp) {
        if (crp == null || !crp.matches(CRP_PATTERN)) {
            throw new BusinessException("Invalid CRP format. Expected: XX/XXXXXX (e.g., 06/123456)");
        }
    }

    private static void validateSpecialty(Specialty specialty) {
        if (specialty == null) {
            throw new BusinessException("Specialty is required");
        }
    }

    public static void validateBaseSessionRate(BigDecimal rate) {
        if (rate == null) {
            throw new BusinessException("Base session rate is required");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Base session rate cannot be negative");
        }
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String crp() { return crp; }
    public Specialty specialty() { return specialty; }
    public BigDecimal baseSessionRate() { return baseSessionRate; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
