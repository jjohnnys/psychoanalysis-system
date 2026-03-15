package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JdbcAppointmentRepository.class, JdbcPatientRepository.class, JdbcPsychologistRepository.class})
@DisplayName("AppointmentRepository Integration Tests")
class AppointmentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("clinicalmind_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    JdbcAppointmentRepository appointmentRepository;

    @Autowired
    JdbcPatientRepository patientRepository;

    @Autowired
    JdbcPsychologistRepository psychologistRepository;

    private Patient patient;
    private Psychologist psychologist;

    @BeforeEach
    void setUp(@Autowired JdbcClient jdbcClient) {
        jdbcClient.sql("DELETE FROM clinical_evolutions").update();
        jdbcClient.sql("DELETE FROM appointments").update();
        jdbcClient.sql("DELETE FROM patients").update();
        jdbcClient.sql("DELETE FROM psychologists").update();

        patient = Patient.create("João Silva", "52998224725",
                LocalDate.of(1990, 5, 15), "joao@example.com", "11999999999", null);
        psychologist = Psychologist.create("Dra. Ana Costa", "06/123456",
                Specialty.PSICANALISE, new BigDecimal("200.00"));

        patientRepository.save(patient);
        psychologistRepository.save(psychologist);
    }

    private Appointment buildFutureAppointment() {
        return Appointment.schedule(patient.id(), psychologist.id(),
                LocalDateTime.now().plusHours(2), new BigDecimal("200.00"));
    }

    @Nested
    @DisplayName("Save and Find")
    class SaveAndFind {

        @Test
        @DisplayName("should save and find appointment by id")
        void shouldSaveAndFindById() {
            var appointment = buildFutureAppointment();
            appointmentRepository.save(appointment);

            var found = appointmentRepository.findById(appointment.id());

            assertThat(found).isPresent();
            assertThat(found.get().patientId()).isEqualTo(patient.id());
            assertThat(found.get().psychologistId()).isEqualTo(psychologist.id());
            assertThat(found.get().status()).isEqualTo(AppointmentStatus.AGENDADA);
            assertThat(found.get().appliedValue()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            assertThat(appointmentRepository.findById(UUID.randomUUID())).isEmpty();
        }

        @Test
        @DisplayName("should find appointments by patient id")
        void shouldFindByPatientId() {
            appointmentRepository.save(buildFutureAppointment());
            appointmentRepository.save(Appointment.schedule(patient.id(), psychologist.id(),
                    LocalDateTime.now().plusHours(4), new BigDecimal("200.00")));

            var found = appointmentRepository.findByPatientId(patient.id());
            assertThat(found).hasSize(2);
        }

        @Test
        @DisplayName("should find appointments by psychologist id")
        void shouldFindByPsychologistId() {
            appointmentRepository.save(buildFutureAppointment());

            var found = appointmentRepository.findByPsychologistId(psychologist.id());
            assertThat(found).hasSize(1);
        }

        @Test
        @DisplayName("should find appointments by status")
        void shouldFindByStatus() {
            var appointment = buildFutureAppointment();
            appointmentRepository.save(appointment);

            var scheduled = appointmentRepository.findByStatus(AppointmentStatus.AGENDADA);
            assertThat(scheduled).hasSize(1);

            appointmentRepository.update(appointment.cancel());

            var cancelled = appointmentRepository.findByStatus(AppointmentStatus.CANCELADA);
            assertThat(cancelled).hasSize(1);
            assertThat(appointmentRepository.findByStatus(AppointmentStatus.AGENDADA)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("should update appointment status to cancelled")
        void shouldUpdateStatusToCancelled() {
            var appointment = buildFutureAppointment();
            appointmentRepository.save(appointment);

            appointmentRepository.update(appointment.cancel());

            var found = appointmentRepository.findById(appointment.id()).orElseThrow();
            assertThat(found.status()).isEqualTo(AppointmentStatus.CANCELADA);
        }
    }

    @Nested
    @DisplayName("Overlap detection")
    class OverlapDetection {

        @Test
        @DisplayName("should detect overlapping appointments for psychologist")
        void shouldDetectOverlap() {
            var start = LocalDateTime.now().plusHours(2);
            var appointment = Appointment.schedule(patient.id(), psychologist.id(),
                    start, start.plusMinutes(50), new BigDecimal("200.00"));
            appointmentRepository.save(appointment);

            var overlappingStart = start.plusMinutes(20);
            var overlappingEnd   = start.plusMinutes(70);

            var hasOverlap = appointmentRepository.hasOverlap(
                    psychologist.id(), overlappingStart, overlappingEnd, null);

            assertThat(hasOverlap).isTrue();
        }

        @Test
        @DisplayName("should not detect overlap for non-overlapping appointments")
        void shouldNotDetectOverlapForNonOverlapping() {
            var start = LocalDateTime.now().plusHours(2);
            var appointment = Appointment.schedule(patient.id(), psychologist.id(),
                    start, start.plusMinutes(50), new BigDecimal("200.00"));
            appointmentRepository.save(appointment);

            var afterEnd    = start.plusMinutes(50);
            var hasOverlap  = appointmentRepository.hasOverlap(
                    psychologist.id(), afterEnd, afterEnd.plusMinutes(50), null);

            assertThat(hasOverlap).isFalse();
        }

        @Test
        @DisplayName("should exclude appointment from overlap check when updating itself")
        void shouldExcludeSelfFromOverlapCheck() {
            var start = LocalDateTime.now().plusHours(2);
            var appointment = Appointment.schedule(patient.id(), psychologist.id(),
                    start, start.plusMinutes(50), new BigDecimal("200.00"));
            appointmentRepository.save(appointment);

            var hasOverlap = appointmentRepository.hasOverlap(
                    psychologist.id(), start, start.plusMinutes(50), appointment.id());

            assertThat(hasOverlap).isFalse();
        }
    }

    @Nested
    @DisplayName("Existence checks")
    class ExistenceChecks {

        @Test
        @DisplayName("should return true when patient has appointments")
        void shouldReturnTrueWhenPatientHasAppointments() {
            appointmentRepository.save(buildFutureAppointment());
            assertThat(appointmentRepository.existsByPatientId(patient.id())).isTrue();
        }

        @Test
        @DisplayName("should return false when patient has no appointments")
        void shouldReturnFalseWhenPatientHasNoAppointments() {
            assertThat(appointmentRepository.existsByPatientId(UUID.randomUUID())).isFalse();
        }
    }
}
