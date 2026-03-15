package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Appointment Domain Tests")
class AppointmentTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID PSYCHOLOGIST_ID = UUID.randomUUID();
    private static final BigDecimal APPLIED_VALUE = new BigDecimal("200.00");
    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusHours(2);

    @Nested
    @DisplayName("Appointment scheduling")
    class Scheduling {

        @Test
        @DisplayName("should schedule appointment with default 50 min duration")
        void shouldScheduleWithDefaultDuration() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);

            assertThat(appointment.id()).isNotNull();
            assertThat(appointment.status()).isEqualTo(AppointmentStatus.AGENDADA);
            assertThat(appointment.endDateTime()).isEqualTo(FUTURE_START.plusMinutes(50));
            assertThat(appointment.patientId()).isEqualTo(PATIENT_ID);
            assertThat(appointment.psychologistId()).isEqualTo(PSYCHOLOGIST_ID);
        }

        @Test
        @DisplayName("should schedule appointment with custom duration")
        void shouldScheduleWithCustomDuration() {
            var endTime = FUTURE_START.plusMinutes(90);
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, endTime, APPLIED_VALUE);

            assertThat(appointment.endDateTime()).isEqualTo(endTime);
        }

        @Test
        @DisplayName("should throw when scheduled less than 1 hour in advance")
        void shouldThrowWhenLessThanOneHourAdvance() {
            var tooSoon = LocalDateTime.now().plusMinutes(30);

            assertThatThrownBy(() -> Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, tooSoon, APPLIED_VALUE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("at least 1 hour");
        }

        @Test
        @DisplayName("should throw when end time is before start time")
        void shouldThrowWhenEndTimeBeforeStartTime() {
            var endBeforeStart = FUTURE_START.minusMinutes(10);

            assertThatThrownBy(() -> Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, endBeforeStart, APPLIED_VALUE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End date/time must be after");
        }

        @Test
        @DisplayName("should throw when applied value is negative")
        void shouldThrowWhenAppliedValueIsNegative() {
            assertThatThrownBy(() -> Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, new BigDecimal("-1")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("should throw when patient id is null")
        void shouldThrowWhenPatientIdIsNull() {
            assertThatThrownBy(() -> Appointment.schedule(null, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Patient ID is required");
        }
    }

    @Nested
    @DisplayName("Overlap detection")
    class OverlapDetection {

        @Test
        @DisplayName("should detect overlap when new appointment starts inside existing one")
        void shouldDetectOverlapWhenStartsInside() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);
            var overlapStart = FUTURE_START.plusMinutes(20);
            var overlapEnd = FUTURE_START.plusMinutes(70);

            assertThat(appointment.overlapsWith(overlapStart, overlapEnd)).isTrue();
        }

        @Test
        @DisplayName("should detect overlap when new appointment fully contains existing one")
        void shouldDetectOverlapWhenFullyContains() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);

            assertThat(appointment.overlapsWith(FUTURE_START.minusMinutes(10), FUTURE_START.plusMinutes(60))).isTrue();
        }

        @Test
        @DisplayName("should not detect overlap for non-overlapping appointments")
        void shouldNotDetectOverlapForNonOverlapping() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);
            var afterEnd = FUTURE_START.plusMinutes(50);

            assertThat(appointment.overlapsWith(afterEnd, afterEnd.plusMinutes(50))).isFalse();
        }

        @Test
        @DisplayName("should not detect overlap for adjacent appointments")
        void shouldNotDetectOverlapForAdjacent() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);
            var exactEnd = FUTURE_START.plusMinutes(50);

            assertThat(appointment.overlapsWith(exactEnd, exactEnd.plusMinutes(50))).isFalse();
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class StatusTransitions {

        @Test
        @DisplayName("should cancel a scheduled appointment")
        void shouldCancelScheduledAppointment() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);

            var cancelled = appointment.cancel();

            assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELADA);
            assertThat(cancelled.id()).isEqualTo(appointment.id());
        }

        @Test
        @DisplayName("should throw when cancelling already cancelled appointment")
        void shouldThrowWhenCancellingAlreadyCancelled() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);
            var cancelled = appointment.cancel();

            assertThatThrownBy(cancelled::cancel)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("should throw when completing appointment before start time")
        void shouldThrowWhenCompletingBeforeStartTime() {
            var appointment = Appointment.schedule(PATIENT_ID, PSYCHOLOGIST_ID, FUTURE_START, APPLIED_VALUE);

            assertThatThrownBy(appointment::markAsCompleted)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("after its start time");
        }

        @Test
        @DisplayName("should throw when completing a cancelled appointment")
        void shouldThrowWhenCompletingCancelledAppointment() {
            var pastStart = LocalDateTime.now().minusHours(1);
            var pastEnd = pastStart.plusMinutes(50);
            var appointment = Appointment.reconstitute(
                    UUID.randomUUID(), PATIENT_ID, PSYCHOLOGIST_ID,
                    pastStart, pastEnd, AppointmentStatus.CANCELADA,
                    APPLIED_VALUE, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2));

            assertThatThrownBy(appointment::markAsCompleted)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot complete a cancelled");
        }

        @Test
        @DisplayName("should mark past appointment as completed")
        void shouldMarkPastAppointmentAsCompleted() {
            var pastStart = LocalDateTime.now().minusHours(2);
            var pastEnd = pastStart.plusMinutes(50);
            var appointment = Appointment.reconstitute(
                    UUID.randomUUID(), PATIENT_ID, PSYCHOLOGIST_ID,
                    pastStart, pastEnd, AppointmentStatus.AGENDADA,
                    APPLIED_VALUE, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(3));

            var completed = appointment.markAsCompleted();

            assertThat(completed.status()).isEqualTo(AppointmentStatus.REALIZADA);
        }

        @Test
        @DisplayName("should mark appointment as absence")
        void shouldMarkAsAbsence() {
            var pastStart = LocalDateTime.now().minusHours(1);
            var pastEnd = pastStart.plusMinutes(50);
            var appointment = Appointment.reconstitute(
                    UUID.randomUUID(), PATIENT_ID, PSYCHOLOGIST_ID,
                    pastStart, pastEnd, AppointmentStatus.AGENDADA,
                    APPLIED_VALUE, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2));

            var absence = appointment.markAsAbsence();

            assertThat(absence.status()).isEqualTo(AppointmentStatus.AUSENCIA);
        }

        @Test
        @DisplayName("should throw when cancelling a completed appointment")
        void shouldThrowWhenCancellingCompletedAppointment() {
            var pastStart = LocalDateTime.now().minusHours(2);
            var pastEnd = pastStart.plusMinutes(50);
            var appointment = Appointment.reconstitute(
                    UUID.randomUUID(), PATIENT_ID, PSYCHOLOGIST_ID,
                    pastStart, pastEnd, AppointmentStatus.REALIZADA,
                    APPLIED_VALUE, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(3));

            assertThatThrownBy(appointment::cancel)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel a completed");
        }
    }
}
