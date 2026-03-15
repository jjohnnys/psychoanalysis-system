package com.jjohnnys.psa.domain;

import com.jjohnnys.psa.domain.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Psychologist Domain Tests")
class PsychologistTest {

    private static final String VALID_CRP = "06/123456";
    private static final String VALID_NAME = "Dra. Ana Costa";
    private static final BigDecimal VALID_RATE = new BigDecimal("200.00");

    @Nested
    @DisplayName("Psychologist creation")
    class Creation {

        @Test
        @DisplayName("should create psychologist with valid data")
        void shouldCreateWithValidData() {
            var psychologist = Psychologist.create(VALID_NAME, VALID_CRP, Specialty.PSICANALISE, VALID_RATE);

            assertThat(psychologist.id()).isNotNull();
            assertThat(psychologist.name()).isEqualTo(VALID_NAME);
            assertThat(psychologist.crp()).isEqualTo(VALID_CRP);
            assertThat(psychologist.specialty()).isEqualTo(Specialty.PSICANALISE);
            assertThat(psychologist.baseSessionRate()).isEqualByComparingTo(VALID_RATE);
            assertThat(psychologist.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            assertThatThrownBy(() -> Psychologist.create("", VALID_CRP, Specialty.TCC, VALID_RATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("should throw when specialty is null")
        void shouldThrowWhenSpecialtyIsNull() {
            assertThatThrownBy(() -> Psychologist.create(VALID_NAME, VALID_CRP, null, VALID_RATE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Specialty is required");
        }
    }

    @Nested
    @DisplayName("CRP validation")
    class CrpValidation {

        @Test
        @DisplayName("should accept valid CRP format XX/XXXXXX")
        void shouldAcceptValidCrpFormat() {
            assertThatCode(() -> Psychologist.validateCrp("06/123456")).doesNotThrowAnyException();
            assertThatCode(() -> Psychologist.validateCrp("01/1234")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when CRP is null")
        void shouldThrowWhenCrpIsNull() {
            assertThatThrownBy(() -> Psychologist.validateCrp(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid CRP format");
        }

        @Test
        @DisplayName("should throw when CRP has wrong format")
        void shouldThrowWhenCrpHasWrongFormat() {
            assertThatThrownBy(() -> Psychologist.validateCrp("6/123456"))
                    .isInstanceOf(BusinessException.class);
            assertThatThrownBy(() -> Psychologist.validateCrp("06-123456"))
                    .isInstanceOf(BusinessException.class);
            assertThatThrownBy(() -> Psychologist.validateCrp("ABC/123456"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Financial consistency")
    class FinancialConsistency {

        @Test
        @DisplayName("should allow zero session rate")
        void shouldAllowZeroSessionRate() {
            assertThatCode(() -> Psychologist.create(VALID_NAME, VALID_CRP, Specialty.TCC, BigDecimal.ZERO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when session rate is negative")
        void shouldThrowWhenRateIsNegative() {
            assertThatThrownBy(() -> Psychologist.validateBaseSessionRate(new BigDecimal("-0.01")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("should throw when session rate is null")
        void shouldThrowWhenRateIsNull() {
            assertThatThrownBy(() -> Psychologist.validateBaseSessionRate(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("required");
        }

        @Test
        @DisplayName("should update session rate returning new instance")
        void shouldUpdateSessionRateReturningNewInstance() {
            var psychologist = Psychologist.create(VALID_NAME, VALID_CRP, Specialty.TCC, VALID_RATE);
            var newRate = new BigDecimal("250.00");

            var updated = psychologist.updateBaseSessionRate(newRate);

            assertThat(updated.baseSessionRate()).isEqualByComparingTo(newRate);
            assertThat(updated.id()).isEqualTo(psychologist.id());
            assertThat(updated.crp()).isEqualTo(psychologist.crp());
            assertThat(psychologist.baseSessionRate()).isEqualByComparingTo(VALID_RATE);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("should return new instance on update preserving crp")
        void shouldReturnNewInstanceOnUpdate() {
            var psychologist = Psychologist.create(VALID_NAME, VALID_CRP, Specialty.TCC, VALID_RATE);

            var updated = psychologist.updateName("Dr. Carlos Lima");

            assertThat(updated).isNotSameAs(psychologist);
            assertThat(updated.crp()).isEqualTo(VALID_CRP);
            assertThat(updated.name()).isEqualTo("Dr. Carlos Lima");
        }
    }
}
