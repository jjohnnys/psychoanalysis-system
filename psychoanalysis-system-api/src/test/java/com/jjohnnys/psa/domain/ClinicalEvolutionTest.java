package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ClinicalEvolution Domain Tests")
class ClinicalEvolutionTest {

    private static final UUID APPOINTMENT_ID = UUID.randomUUID();
    private static final UUID PSYCHOLOGIST_ID = UUID.randomUUID();
    private static final UUID OTHER_PSYCHOLOGIST_ID = UUID.randomUUID();
    private static final String CONTENT = "Paciente relatou melhora significativa nos quadros de ansiedade.";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create clinical evolution with valid data")
        void shouldCreateWithValidData() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);

            assertThat(evolution.id()).isNotNull();
            assertThat(evolution.appointmentId()).isEqualTo(APPOINTMENT_ID);
            assertThat(evolution.psychologistId()).isEqualTo(PSYCHOLOGIST_ID);
            assertThat(evolution.content()).isEqualTo(CONTENT);
            assertThat(evolution.createdAt()).isNotNull();
            assertThat(evolution.isClosed()).isFalse();
        }

        @Test
        @DisplayName("should throw when content is blank")
        void shouldThrowWhenContentIsBlank() {
            assertThatThrownBy(() -> ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("content is required");
        }

        @Test
        @DisplayName("should throw when appointment id is null")
        void shouldThrowWhenAppointmentIdIsNull() {
            assertThatThrownBy(() -> ClinicalEvolution.create(null, PSYCHOLOGIST_ID, CONTENT))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Appointment ID is required");
        }

        @Test
        @DisplayName("should throw when psychologist id is null")
        void shouldThrowWhenPsychologistIdIsNull() {
            assertThatThrownBy(() -> ClinicalEvolution.create(APPOINTMENT_ID, null, CONTENT))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Psychologist ID is required");
        }
    }

    @Nested
    @DisplayName("Edit within 24 hours (open)")
    class EditOpen {

        @Test
        @DisplayName("should allow edit by linked psychologist within 24h")
        void shouldAllowEditByLinkedPsychologist() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);
            var newContent = "Conteúdo atualizado com mais detalhes clínicos.";

            var edited = evolution.edit(newContent, PSYCHOLOGIST_ID);

            assertThat(edited.content()).isEqualTo(newContent);
            assertThat(edited.id()).isEqualTo(evolution.id());
        }

        @Test
        @DisplayName("should throw when edit requested by different psychologist")
        void shouldThrowWhenEditByDifferentPsychologist() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);

            assertThatThrownBy(() -> evolution.edit("novo conteúdo", OTHER_PSYCHOLOGIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Only the linked psychologist");
        }

        @Test
        @DisplayName("should throw when editing closed evolution")
        void shouldThrowWhenEditingClosedEvolution() {
            var closedEvolution = ClinicalEvolution.reconstitute(
                    UUID.randomUUID(), APPOINTMENT_ID, PSYCHOLOGIST_ID,
                    CONTENT,
                    LocalDateTime.now().minusHours(25),
                    LocalDateTime.now().minusHours(25)
            );

            assertThatThrownBy(() -> closedEvolution.edit("novo conteúdo", PSYCHOLOGIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("closed after 24 hours");
        }
    }

    @Nested
    @DisplayName("Rectification after 24 hours (closed)")
    class Rectification {

        @Test
        @DisplayName("should allow rectification by linked psychologist after 24h")
        void shouldAllowRectificationByLinkedPsychologist() {
            var closedEvolution = ClinicalEvolution.reconstitute(
                    UUID.randomUUID(), APPOINTMENT_ID, PSYCHOLOGIST_ID,
                    CONTENT,
                    LocalDateTime.now().minusHours(25),
                    LocalDateTime.now().minusHours(25)
            );

            var rectified = closedEvolution.rectify("Informação complementar adicionada.", PSYCHOLOGIST_ID);

            assertThat(rectified.content()).contains(CONTENT);
            assertThat(rectified.content()).contains("RETIFICAÇÃO");
            assertThat(rectified.content()).contains("Informação complementar adicionada.");
        }

        @Test
        @DisplayName("should throw when rectification requested by different psychologist")
        void shouldThrowWhenRectificationByDifferentPsychologist() {
            var closedEvolution = ClinicalEvolution.reconstitute(
                    UUID.randomUUID(), APPOINTMENT_ID, PSYCHOLOGIST_ID,
                    CONTENT,
                    LocalDateTime.now().minusHours(25),
                    LocalDateTime.now().minusHours(25)
            );

            assertThatThrownBy(() -> closedEvolution.rectify("retificação", OTHER_PSYCHOLOGIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Only the linked psychologist");
        }

        @Test
        @DisplayName("should throw when rectifying open evolution")
        void shouldThrowWhenRectifyingOpenEvolution() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);

            assertThatThrownBy(() -> evolution.rectify("retificação", PSYCHOLOGIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("only allowed after the evolution is closed");
        }
    }

    @Nested
    @DisplayName("Closed state detection")
    class ClosedState {

        @Test
        @DisplayName("should be open when created less than 24 hours ago")
        void shouldBeOpenWhenCreatedRecentlyAgo() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);
            assertThat(evolution.isClosed()).isFalse();
        }

        @Test
        @DisplayName("should be closed when created more than 24 hours ago")
        void shouldBeClosedWhenCreatedMoreThan24HoursAgo() {
            var evolution = ClinicalEvolution.reconstitute(
                    UUID.randomUUID(), APPOINTMENT_ID, PSYCHOLOGIST_ID,
                    CONTENT,
                    LocalDateTime.now().minusHours(25),
                    LocalDateTime.now().minusHours(25)
            );
            assertThat(evolution.isClosed()).isTrue();
        }

        @Test
        @DisplayName("should throw when editing with null psychologist id")
        void shouldThrowWhenEditingWithNullPsychologistId() {
            var evolution = ClinicalEvolution.create(APPOINTMENT_ID, PSYCHOLOGIST_ID, CONTENT);

            assertThatThrownBy(() -> evolution.edit("novo conteúdo", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Only the linked psychologist");
        }
    }
}
