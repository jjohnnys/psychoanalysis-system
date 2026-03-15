package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.application.repository.PatientRepository;
import com.jjohnnys.psa.domain.Patient;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPatientRepository implements PatientRepository {

    private final JdbcClient jdbcClient;

    public JdbcPatientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Patient save(Patient patient) {
        jdbcClient.sql("""
                INSERT INTO patients (id, name, cpf, birth_date, email, phone, guardian_id, active, created_at, updated_at)
                VALUES (:id, :name, :cpf, :birthDate, :email, :phone, :guardianId, :active, :createdAt, :updatedAt)
                """)
                .param("id", patient.id())
                .param("name", patient.name())
                .param("cpf", patient.cpf())
                .param("birthDate", patient.birthDate())
                .param("email", patient.email())
                .param("phone", patient.phone())
                .param("guardianId", patient.guardianId())
                .param("active", patient.active())
                .param("createdAt", patient.createdAt())
                .param("updatedAt", patient.updatedAt())
                .update();
        return patient;
    }

    @Override
    public Patient update(Patient patient) {
        jdbcClient.sql("""
                UPDATE patients
                SET name = :name, email = :email, phone = :phone,
                    guardian_id = :guardianId, active = :active, updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", patient.id())
                .param("name", patient.name())
                .param("email", patient.email())
                .param("phone", patient.phone())
                .param("guardianId", patient.guardianId())
                .param("active", patient.active())
                .param("updatedAt", patient.updatedAt())
                .update();
        return patient;
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return jdbcClient.sql("SELECT * FROM patients WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public Optional<Patient> findByCpf(String cpf) {
        String sanitized = cpf.replaceAll("\\D", "");
        return jdbcClient.sql("SELECT * FROM patients WHERE cpf = :cpf")
                .param("cpf", sanitized)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public Optional<Patient> findByEmail(String email) {
        return jdbcClient.sql("SELECT * FROM patients WHERE email = :email")
                .param("email", email)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public List<Patient> findAll() {
        return jdbcClient.sql("SELECT * FROM patients ORDER BY name")
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public List<Patient> findAllActive() {
        return jdbcClient.sql("SELECT * FROM patients WHERE active = true ORDER BY name")
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public boolean existsByCpf(String cpf) {
        String sanitized = cpf.replaceAll("\\D", "");
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM patients WHERE cpf = :cpf)")
                        .param("cpf", sanitized)
                        .query(Boolean.class)
                        .single());
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM patients WHERE email = :email)")
                        .param("email", email)
                        .query(Boolean.class)
                        .single());
    }

    @Override
    public boolean hasAppointments(UUID patientId) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM appointments WHERE patient_id = :patientId)")
                        .param("patientId", patientId)
                        .query(Boolean.class)
                        .single());
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        String guardianIdStr = rs.getString("guardian_id");
        return Patient.reconstitute(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("cpf"),
                rs.getDate("birth_date").toLocalDate(),
                rs.getString("email"),
                rs.getString("phone"),
                guardianIdStr != null ? UUID.fromString(guardianIdStr) : null,
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
