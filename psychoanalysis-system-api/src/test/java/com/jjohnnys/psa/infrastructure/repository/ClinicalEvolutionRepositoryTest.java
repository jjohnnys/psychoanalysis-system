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
@Import({JdbcClinicalEvolutionRepository.class, JdbcAppointmentRepository.class,
        JdbcPatientRepository.class, JdbcPsychologistRepository.class})
@DisplayName("ClinicalEvolutionRepository Integration Tests")
class ClinicalEvolutionRepositoryTest {

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
    JdbcClinicalEvolutionRepository evolutionRepository;

    @Autowired
    JdbcAppointmentRepository appointmentRepository;

    @Autowired
    JdbcPatientRepository patientRepository;

    @Autowired
    JdbcPsychologistRepository psychologistRepository;

    private Appointment appointment;
    private Psychologist psychologist;

    @BeforeEach
    void setUp(@Autowired JdbcClient jdbcClient) {
        jdbcClient.sql("DELETE FROM clinical_evolutions").update();
        jdbcClient.sql("DELETE FROM appointments").update();
        jdbcClient.sql("DELETE FROM patients").update();
        jdbcClient.sql("DELETE FROM psychologists").update();

        var patient = Patient.create("João Silva", "52998224725",
                LocalDate.of(1990, 5, 15), "joao@example.com", "11999999999", null);
        psychologist = Psychologist.create("Dra. Ana Costa", "06/123456",
                Specialty.PSICANALISE, new BigDecimal("200.00"));

        patientRepository.save(patient);
        psychologistRepository.save(psychologist);

        appointment = Appointment.reconstitute(
                UUID.randomUUID(), patient.id(), psychologist.id(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1),
                AppointmentStatus.REALIZADA,
                new BigDecimal("200.00"),
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(2)
        );
        appointmentRepository.save(appointment);
    }

    @Nested
    @DisplayName("Save and Find")
    class SaveAndFind {

        @Test
        @DisplayName("should save and find evolution by id")
        void shouldSaveAndFindById() {
            var evolution = ClinicalEvolution.create(
                    appointment.id(), psychologist.id(),
                    "Paciente demonstrou melhora significativa.");
            evolutionRepository.save(evolution);

            var found = evolutionRepository.findById(evolution.id());

            assertThat(found).isPresent();
            assertThat(found.get().appointmentId()).isEqualTo(appointment.id());
            assertThat(found.get().psychologistId()).isEqualTo(psychologist.id());
            assertThat(found.get().content()).isEqualTo("Paciente demonstrou melhora significativa.");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            assertThat(evolutionRepository.findById(UUID.randomUUID())).isEmpty();
        }

        @Test
        @DisplayName("should find evolution by appointment id")
        void shouldFindByAppointmentId() {
            var evolution = ClinicalEvolution.create(
                    appointment.id(), psychologist.id(), "Conteúdo clínico.");
            evolutionRepository.save(evolution);

            var found = evolutionRepository.findByAppointmentId(appointment.id());

            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(evolution.id());
        }

        @Test
        @DisplayName("should return empty when no evolution for appointment")
        void shouldReturnEmptyWhenNoEvolutionForAppointment() {
            assertThat(evolutionRepository.findByAppointmentId(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("should update evolution content within 24h")
        void shouldUpdateContentWithin24h() {
            var evolution = ClinicalEvolution.create(
                    appointment.id(), psychologist.id(), "Conteúdo original.");
            evolutionRepository.save(evolution);

            var edited = evolution.edit("Conteúdo atualizado.", psychologist.id());
            evolutionRepository.update(edited);

            var found = evolutionRepository.findById(evolution.id()).orElseThrow();
            assertThat(found.content()).isEqualTo("Conteúdo atualizado.");
        }

        @Test
        @DisplayName("should save rectified evolution after 24h")
        void shouldSaveRectifiedEvolutionAfter24h() {
            var closedEvolution = ClinicalEvolution.reconstitute(
                    UUID.randomUUID(), appointment.id(), psychologist.id(),
                    "Conteúdo original.",
                    LocalDateTime.now().minusHours(25),
                    LocalDateTime.now().minusHours(25)
            );
            evolutionRepository.save(closedEvolution);

            var rectified = closedEvolution.rectify("Adendo clínico importante.", psychologist.id());
            evolutionRepository.update(rectified);

            var found = evolutionRepository.findById(closedEvolution.id()).orElseThrow();
            assertThat(found.content()).contains("Conteúdo original.");
            assertThat(found.content()).contains("RETIFICAÇÃO");
            assertThat(found.content()).contains("Adendo clínico importante.");
        }
    }

    @Nested
    @DisplayName("Existence checks")
    class ExistenceChecks {

        @Test
        @DisplayName("should return true when evolution exists for appointment")
        void shouldReturnTrueWhenEvolutionExists() {
            var evolution = ClinicalEvolution.create(
                    appointment.id(), psychologist.id(), "Conteúdo.");
            evolutionRepository.save(evolution);

            assertThat(evolutionRepository.existsByAppointmentId(appointment.id())).isTrue();
        }

        @Test
        @DisplayName("should return false when no evolution for appointment")
        void shouldReturnFalseWhenNoEvolution() {
            assertThat(evolutionRepository.existsByAppointmentId(UUID.randomUUID())).isFalse();
        }
    }
}
