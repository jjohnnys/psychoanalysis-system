package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.domain.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcPatientRepository.class)
@DisplayName("PatientRepository Integration Tests")
class PatientRepositoryTest {

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
    JdbcPatientRepository repository;

    private static final String VALID_CPF      = "52998224725";
    private static final String VALID_CPF_2    = "11144477735";
    private static final String VALID_EMAIL    = "joao@example.com";
    private static final String VALID_EMAIL_2  = "maria@example.com";
    private static final LocalDate BIRTH_DATE  = LocalDate.of(1990, 5, 15);

    private Patient buildPatient(String cpf, String email) {
        return Patient.create("João Silva", cpf, BIRTH_DATE, email, "11999999999", null);
    }

    @BeforeEach
    void cleanUp(@Autowired org.springframework.jdbc.core.simple.JdbcClient jdbcClient) {
        jdbcClient.sql("DELETE FROM appointments").update();
        jdbcClient.sql("DELETE FROM patients").update();
    }

    @Nested
    @DisplayName("Save and Find")
    class SaveAndFind {

        @Test
        @DisplayName("should save and find patient by id")
        void shouldSaveAndFindById() {
            var patient = buildPatient(VALID_CPF, VALID_EMAIL);
            repository.save(patient);

            var found = repository.findById(patient.id());

            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(patient.id());
            assertThat(found.get().name()).isEqualTo("João Silva");
            assertThat(found.get().cpf()).isEqualTo(VALID_CPF);
            assertThat(found.get().email()).isEqualTo(VALID_EMAIL);
            assertThat(found.get().active()).isTrue();
        }

        @Test
        @DisplayName("should return empty when patient not found")
        void shouldReturnEmptyWhenNotFound() {
            var found = repository.findById(UUID.randomUUID());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should find patient by CPF")
        void shouldFindByCpf() {
            var patient = buildPatient(VALID_CPF, VALID_EMAIL);
            repository.save(patient);

            var found = repository.findByCpf(VALID_CPF);

            assertThat(found).isPresent();
            assertThat(found.get().cpf()).isEqualTo(VALID_CPF);
        }

        @Test
        @DisplayName("should find patient by email")
        void shouldFindByEmail() {
            var patient = buildPatient(VALID_CPF, VALID_EMAIL);
            repository.save(patient);

            var found = repository.findByEmail(VALID_EMAIL);

            assertThat(found).isPresent();
            assertThat(found.get().email()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("should find all patients")
        void shouldFindAll() {
            repository.save(buildPatient(VALID_CPF, VALID_EMAIL));
            repository.save(buildPatient(VALID_CPF_2, VALID_EMAIL_2));

            var patients = repository.findAll();

            assertThat(patients).hasSize(2);
        }

        @Test
        @DisplayName("should find only active patients")
        void shouldFindAllActive() {
            var active   = buildPatient(VALID_CPF, VALID_EMAIL);
            var inactive = buildPatient(VALID_CPF_2, VALID_EMAIL_2);
            repository.save(active);
            repository.save(inactive);
            repository.update(inactive.deactivate());

            var actives = repository.findAllActive();

            assertThat(actives).hasSize(1);
            assertThat(actives.get(0).id()).isEqualTo(active.id());
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("should update patient contact info")
        void shouldUpdateContactInfo() {
            var patient = buildPatient(VALID_CPF, VALID_EMAIL);
            repository.save(patient);

            var updated = patient.updateContactInfo("João Santos", "joao.santos@example.com", "11888888888");
            repository.update(updated);

            var found = repository.findById(patient.id()).orElseThrow();
            assertThat(found.name()).isEqualTo("João Santos");
            assertThat(found.email()).isEqualTo("joao.santos@example.com");
            assertThat(found.cpf()).isEqualTo(VALID_CPF);
        }

        @Test
        @DisplayName("should soft-delete patient")
        void shouldSoftDeletePatient() {
            var patient = buildPatient(VALID_CPF, VALID_EMAIL);
            repository.save(patient);

            var deactivated = patient.deactivate();
            repository.update(deactivated);

            var found = repository.findById(patient.id()).orElseThrow();
            assertThat(found.active()).isFalse();
        }
    }

    @Nested
    @DisplayName("Existence checks")
    class ExistenceChecks {

        @Test
        @DisplayName("should return true when CPF exists")
        void shouldReturnTrueWhenCpfExists() {
            repository.save(buildPatient(VALID_CPF, VALID_EMAIL));
            assertThat(repository.existsByCpf(VALID_CPF)).isTrue();
        }

        @Test
        @DisplayName("should return false when CPF does not exist")
        void shouldReturnFalseWhenCpfDoesNotExist() {
            assertThat(repository.existsByCpf(VALID_CPF)).isFalse();
        }

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            repository.save(buildPatient(VALID_CPF, VALID_EMAIL));
            assertThat(repository.existsByEmail(VALID_EMAIL)).isTrue();
        }
    }
}
