package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class Appointment {

    public static final int DEFAULT_DURATION_MINUTES = 50;
    public static final int MIN_ADVANCE_HOURS = 1;

    private final UUID id;
    private final UUID patientId;
    private final UUID psychologistId;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final AppointmentStatus status;
    private final BigDecimal appliedValue;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Appointment(UUID id, UUID patientId, UUID psychologistId,
                        LocalDateTime startDateTime, LocalDateTime endDateTime,
                        AppointmentStatus status, BigDecimal appliedValue,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.psychologistId = psychologistId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
        this.appliedValue = appliedValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Appointment schedule(UUID patientId, UUID psychologistId,
                                       LocalDateTime startDateTime, BigDecimal appliedValue) {
        return schedule(patientId, psychologistId, startDateTime,
                startDateTime.plusMinutes(DEFAULT_DURATION_MINUTES), appliedValue);
    }

    public static Appointment schedule(UUID patientId, UUID psychologistId,
                                       LocalDateTime startDateTime, LocalDateTime endDateTime,
                                       BigDecimal appliedValue) {
        validateIds(patientId, psychologistId);
        validateMinimumAdvance(startDateTime);
        validateTimeRange(startDateTime, endDateTime);
        validateAppliedValue(appliedValue);
        var now = LocalDateTime.now();
        return new Appointment(UUID.randomUUID(), patientId, psychologistId,
                startDateTime, endDateTime, AppointmentStatus.AGENDADA, appliedValue, now, now);
    }

    public static Appointment reconstitute(UUID id, UUID patientId, UUID psychologistId,
                                           LocalDateTime startDateTime, LocalDateTime endDateTime,
                                           AppointmentStatus status, BigDecimal appliedValue,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Appointment(id, patientId, psychologistId, startDateTime, endDateTime,
                status, appliedValue, createdAt, updatedAt);
    }

    public Appointment markAsCompleted() {
        if (status == AppointmentStatus.CANCELADA) {
            throw new BusinessException("Cannot complete a cancelled appointment");
        }
        if (status == AppointmentStatus.REALIZADA) {
            throw new BusinessException("Appointment is already completed");
        }
        if (!LocalDateTime.now().isAfter(startDateTime)) {
            throw new BusinessException("Appointment can only be marked as completed after its start time");
        }
        return new Appointment(id, patientId, psychologistId, startDateTime, endDateTime,
                AppointmentStatus.REALIZADA, appliedValue, createdAt, LocalDateTime.now());
    }

    public Appointment cancel() {
        if (status == AppointmentStatus.CANCELADA) {
            throw new BusinessException("Appointment is already cancelled");
        }
        if (status == AppointmentStatus.REALIZADA) {
            throw new BusinessException("Cannot cancel a completed appointment");
        }
        return new Appointment(id, patientId, psychologistId, startDateTime, endDateTime,
                AppointmentStatus.CANCELADA, appliedValue, createdAt, LocalDateTime.now());
    }

    public Appointment markAsAbsence() {
        if (status == AppointmentStatus.CANCELADA) {
            throw new BusinessException("Cannot mark as absence a cancelled appointment");
        }
        if (status == AppointmentStatus.REALIZADA) {
            throw new BusinessException("Cannot mark as absence a completed appointment");
        }
        return new Appointment(id, patientId, psychologistId, startDateTime, endDateTime,
                AppointmentStatus.AUSENCIA, appliedValue, createdAt, LocalDateTime.now());
    }

    public boolean overlapsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startDateTime.isBefore(otherEnd) && endDateTime.isAfter(otherStart);
    }

    public boolean isScheduled() {
        return status == AppointmentStatus.AGENDADA;
    }

    private static void validateIds(UUID patientId, UUID psychologistId) {
        if (patientId == null) {
            throw new BusinessException("Patient ID is required");
        }
        if (psychologistId == null) {
            throw new BusinessException("Psychologist ID is required");
        }
    }

    private static void validateMinimumAdvance(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new BusinessException("Start date/time is required");
        }
        if (startDateTime.isBefore(LocalDateTime.now().plusHours(MIN_ADVANCE_HOURS))) {
            throw new BusinessException(
                    "Appointment must be scheduled at least " + MIN_ADVANCE_HOURS + " hour(s) in advance");
        }
    }

    private static void validateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (endDateTime == null) {
            throw new BusinessException("End date/time is required");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            throw new BusinessException("End date/time must be after start date/time");
        }
    }

    private static void validateAppliedValue(BigDecimal appliedValue) {
        if (appliedValue == null) {
            throw new BusinessException("Applied value is required");
        }
        if (appliedValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Applied value cannot be negative");
        }
    }

    public UUID id() { return id; }
    public UUID patientId() { return patientId; }
    public UUID psychologistId() { return psychologistId; }
    public LocalDateTime startDateTime() { return startDateTime; }
    public LocalDateTime endDateTime() { return endDateTime; }
    public AppointmentStatus status() { return status; }
    public BigDecimal appliedValue() { return appliedValue; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
