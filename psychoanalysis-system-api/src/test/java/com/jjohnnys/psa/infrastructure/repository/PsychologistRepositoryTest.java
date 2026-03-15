package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.domain.Psychologist;
import com.jjohnnys.psa.domain.Specialty;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcPsychologistRepository.class)
@DisplayName("PsychologistRepository Integration Tests")
class PsychologistRepositoryTest {

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
    JdbcPsychologistRepository repository;

    @BeforeEach
    void cleanUp(@Autowired JdbcClient jdbcClient) {
        jdbcClient.sql("DELETE FROM clinical_evolutions").update();
        jdbcClient.sql("DELETE FROM appointments").update();
        jdbcClient.sql("DELETE FROM psychologists").update();
    }

    private Psychologist buildPsychologist(String crp) {
        return Psychologist.create("Dra. Ana Costa", crp, Specialty.PSICANALISE, new BigDecimal("200.00"));
    }

    @Nested
    @DisplayName("Save and Find")
    class SaveAndFind {

        @Test
        @DisplayName("should save and find psychologist by id")
        void shouldSaveAndFindById() {
            var psychologist = buildPsychologist("06/123456");
            repository.save(psychologist);

            var found = repository.findById(psychologist.id());

            assertThat(found).isPresent();
            assertThat(found.get().name()).isEqualTo("Dra. Ana Costa");
            assertThat(found.get().crp()).isEqualTo("06/123456");
            assertThat(found.get().specialty()).isEqualTo(Specialty.PSICANALISE);
            assertThat(found.get().baseSessionRate()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            assertThat(repository.findById(UUID.randomUUID())).isEmpty();
        }

        @Test
        @DisplayName("should find by CRP")
        void shouldFindByCrp() {
            repository.save(buildPsychologist("06/123456"));

            var found = repository.findByCrp("06/123456");

            assertThat(found).isPresent();
            assertThat(found.get().crp()).isEqualTo("06/123456");
        }

        @Test
        @DisplayName("should find all psychologists")
        void shouldFindAll() {
            repository.save(buildPsychologist("06/111111"));
            repository.save(buildPsychologist("06/222222"));

            assertThat(repository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("should find psychologists by specialty")
        void shouldFindBySpecialty() {
            repository.save(buildPsychologist("06/111111"));
            repository.save(
                    Psychologist.create("Dr. Carlos", "06/222222", Specialty.TCC, new BigDecimal("180.00")));

            var psicanalistasFound = repository.findBySpecialty(Specialty.PSICANALISE);
            var tccFound           = repository.findBySpecialty(Specialty.TCC);

            assertThat(psicanalistasFound).hasSize(1);
            assertThat(tccFound).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("should update session rate")
        void shouldUpdateSessionRate() {
            var psychologist = buildPsychologist("06/123456");
            repository.save(psychologist);

            var updated = psychologist.updateBaseSessionRate(new BigDecimal("250.00"));
            repository.update(updated);

            var found = repository.findById(psychologist.id()).orElseThrow();
            assertThat(found.baseSessionRate()).isEqualByComparingTo("250.00");
            assertThat(found.crp()).isEqualTo("06/123456");
        }
    }

    @Nested
    @DisplayName("Existence checks")
    class ExistenceChecks {

        @Test
        @DisplayName("should return true when CRP exists")
        void shouldReturnTrueWhenCrpExists() {
            repository.save(buildPsychologist("06/123456"));
            assertThat(repository.existsByCrp("06/123456")).isTrue();
        }

        @Test
        @DisplayName("should return false when CRP does not exist")
        void shouldReturnFalseWhenCrpDoesNotExist() {
            assertThat(repository.existsByCrp("06/999999")).isFalse();
        }
    }
}
