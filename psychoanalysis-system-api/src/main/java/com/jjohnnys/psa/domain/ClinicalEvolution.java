package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ClinicalEvolution {

    private final UUID id;
    private final UUID appointmentId;
    private final UUID psychologistId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ClinicalEvolution(UUID id, UUID appointmentId, UUID psychologistId,
                               String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.psychologistId = psychologistId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClinicalEvolution create(UUID appointmentId, UUID psychologistId, String content) {
        validateAppointmentId(appointmentId);
        validatePsychologistId(psychologistId);
        validateContent(content);
        var now = LocalDateTime.now();
        return new ClinicalEvolution(UUID.randomUUID(), appointmentId, psychologistId, content, now, now);
    }

    public static ClinicalEvolution reconstitute(UUID id, UUID appointmentId, UUID psychologistId,
                                                  String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ClinicalEvolution(id, appointmentId, psychologistId, content, createdAt, updatedAt);
    }

    public ClinicalEvolution edit(String newContent, UUID requestingPsychologistId) {
        validateOwnership(requestingPsychologistId);
        if (isClosed()) {
            throw new BusinessException(
                    "Clinical evolution is closed after 24 hours and cannot be edited, only rectified");
        }
        validateContent(newContent);
        return new ClinicalEvolution(id, appointmentId, psychologistId, newContent, createdAt, LocalDateTime.now());
    }

    public ClinicalEvolution rectify(String rectification, UUID requestingPsychologistId) {
        validateOwnership(requestingPsychologistId);
        if (!isClosed()) {
            throw new BusinessException(
                    "Rectification is only allowed after the evolution is closed (24 hours after creation)");
        }
        validateContent(rectification);
        String rectifiedContent = content
                + "\n\n[RETIFICAÇÃO - " + LocalDateTime.now() + "]\n"
                + rectification;
        return new ClinicalEvolution(id, appointmentId, psychologistId, rectifiedContent, createdAt, LocalDateTime.now());
    }

    public boolean isClosed() {
        return LocalDateTime.now().isAfter(createdAt.plusHours(24));
    }

    private void validateOwnership(UUID requestingPsychologistId) {
        if (requestingPsychologistId == null || !requestingPsychologistId.equals(psychologistId)) {
            throw new BusinessException(
                    "Only the linked psychologist can modify this clinical evolution");
        }
    }

    private static void validateAppointmentId(UUID appointmentId) {
        if (appointmentId == null) {
            throw new BusinessException("Appointment ID is required");
        }
    }

    private static void validatePsychologistId(UUID psychologistId) {
        if (psychologistId == null) {
            throw new BusinessException("Psychologist ID is required");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("Clinical evolution content is required");
        }
    }

    public UUID id() { return id; }
    public UUID appointmentId() { return appointmentId; }
    public UUID psychologistId() { return psychologistId; }
    public String content() { return content; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
