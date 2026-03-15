package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Patient Domain Tests")
class PatientTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";
    private static final String VALID_EMAIL = "joao@example.com";
    private static final LocalDate ADULT_BIRTH_DATE = LocalDate.of(1990, 5, 15);
    private static final LocalDate MINOR_BIRTH_DATE = LocalDate.now().minusYears(10);

    @Nested
    @DisplayName("Patient creation")
    class Creation {

        @Test
        @DisplayName("should create adult patient without guardian")
        void shouldCreateAdultPatientWithoutGuardian() {
            var patient = Patient.create("João Silva", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, "11999999999", null);

            assertThat(patient.id()).isNotNull();
            assertThat(patient.name()).isEqualTo("João Silva");
            assertThat(patient.cpf()).isEqualTo(VALID_CPF);
            assertThat(patient.email()).isEqualTo(VALID_EMAIL);
            assertThat(patient.active()).isTrue();
            assertThat(patient.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("should accept formatted CPF and sanitize it")
        void shouldAcceptFormattedCpfAndSanitize() {
            var patient = Patient.create("João Silva", VALID_CPF_FORMATTED, ADULT_BIRTH_DATE, VALID_EMAIL, null, null);

            assertThat(patient.cpf()).isEqualTo(VALID_CPF);
        }

        @Test
        @DisplayName("should create minor patient with guardian")
        void shouldCreateMinorPatientWithGuardian() {
            var guardianId = UUID.randomUUID();
            var patient = Patient.create("Maria Júnior", VALID_CPF, MINOR_BIRTH_DATE, VALID_EMAIL, null, guardianId);

            assertThat(patient.guardianId()).isEqualTo(guardianId);
            assertThat(patient.isMinor()).isTrue();
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            assertThatThrownBy(() -> Patient.create("", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("should throw when email is invalid")
        void shouldThrowWhenEmailIsInvalid() {
            assertThatThrownBy(() -> Patient.create("João", VALID_CPF, ADULT_BIRTH_DATE, "invalid-email", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid email");
        }

        @Test
        @DisplayName("should throw when birth date is in the future")
        void shouldThrowWhenBirthDateIsInFuture() {
            assertThatThrownBy(() -> Patient.create("João", VALID_CPF, LocalDate.now().plusDays(1), VALID_EMAIL, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("future");
        }
    }

    @Nested
    @DisplayName("Minor validation")
    class MinorValidation {

        @Test
        @DisplayName("should throw when minor has no guardian")
        void shouldThrowWhenMinorHasNoGuardian() {
            assertThatThrownBy(() -> Patient.create("Criança", VALID_CPF, MINOR_BIRTH_DATE, VALID_EMAIL, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("guardian is required");
        }

        @Test
        @DisplayName("should not allow guardian assignment for adult patient")
        void shouldNotAllowGuardianForAdultPatient() {
            var patient = Patient.create("João Silva", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, null, null);

            assertThatThrownBy(() -> patient.assignGuardian(UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("only allowed for patients under 18");
        }
    }

    @Nested
    @DisplayName("CPF validation")
    class CpfValidation {

        @Test
        @DisplayName("should throw when CPF has wrong length")
        void shouldThrowWhenCpfHasWrongLength() {
            assertThatThrownBy(() -> Patient.validateCpf("1234567890"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("11 digits");
        }

        @Test
        @DisplayName("should throw when CPF has all same digits")
        void shouldThrowWhenCpfHasAllSameDigits() {
            assertThatThrownBy(() -> Patient.validateCpf("11111111111"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("all digits are the same");
        }

        @Test
        @DisplayName("should throw when CPF checksum is invalid")
        void shouldThrowWhenCpfChecksumIsInvalid() {
            assertThatThrownBy(() -> Patient.validateCpf("12345678901"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("checksum failed");
        }

        @Test
        @DisplayName("should accept valid CPF without formatting")
        void shouldAcceptValidCpfWithoutFormatting() {
            assertThatCode(() -> Patient.validateCpf(VALID_CPF)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept valid CPF with formatting")
        void shouldAcceptValidCpfWithFormatting() {
            assertThatCode(() -> Patient.validateCpf(VALID_CPF_FORMATTED)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Soft delete")
    class SoftDelete {

        @Test
        @DisplayName("should deactivate active patient")
        void shouldDeactivateActivePatient() {
            var patient = Patient.create("João Silva", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, null, null);

            var deactivated = patient.deactivate();

            assertThat(deactivated.active()).isFalse();
            assertThat(deactivated.id()).isEqualTo(patient.id());
            assertThat(deactivated.cpf()).isEqualTo(patient.cpf());
        }

        @Test
        @DisplayName("should throw when deactivating already inactive patient")
        void shouldThrowWhenDeactivatingAlreadyInactive() {
            var patient = Patient.create("João Silva", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, null, null);
            var inactive = patient.deactivate();

            assertThatThrownBy(inactive::deactivate)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already inactive");
        }
    }

    @Nested
    @DisplayName("Identity immutability")
    class IdentityImmutability {

        @Test
        @DisplayName("should preserve id and cpf on contact update")
        void shouldPreserveIdAndCpfOnContactUpdate() {
            var patient = Patient.create("João Silva", VALID_CPF, ADULT_BIRTH_DATE, VALID_EMAIL, null, null);

            var updated = patient.updateContactInfo("João Santos", "joao.santos@example.com", "11888888888");

            assertThat(updated.id()).isEqualTo(patient.id());
            assertThat(updated.cpf()).isEqualTo(patient.cpf());
            assertThat(updated.name()).isEqualTo("João Santos");
        }
    }
}
