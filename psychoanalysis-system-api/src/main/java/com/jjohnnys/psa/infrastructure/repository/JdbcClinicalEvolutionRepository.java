package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.application.repository.ClinicalEvolutionRepository;
import com.jjohnnys.psa.domain.ClinicalEvolution;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcClinicalEvolutionRepository implements ClinicalEvolutionRepository {

    private final JdbcClient jdbcClient;

    public JdbcClinicalEvolutionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public ClinicalEvolution save(ClinicalEvolution evolution) {
        jdbcClient.sql("""
                INSERT INTO clinical_evolutions (id, appointment_id, psychologist_id, content, created_at, updated_at)
                VALUES (:id, :appointmentId, :psychologistId, :content, :createdAt, :updatedAt)
                """)
                .param("id", evolution.id())
                .param("appointmentId", evolution.appointmentId())
                .param("psychologistId", evolution.psychologistId())
                .param("content", evolution.content())
                .param("createdAt", evolution.createdAt())
                .param("updatedAt", evolution.updatedAt())
                .update();
        return evolution;
    }

    @Override
    public ClinicalEvolution update(ClinicalEvolution evolution) {
        jdbcClient.sql("""
                UPDATE clinical_evolutions
                SET content = :content, updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", evolution.id())
                .param("content", evolution.content())
                .param("updatedAt", evolution.updatedAt())
                .update();
        return evolution;
    }

    @Override
    public Optional<ClinicalEvolution> findById(UUID id) {
        return jdbcClient.sql("SELECT * FROM clinical_evolutions WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public Optional<ClinicalEvolution> findByAppointmentId(UUID appointmentId) {
        return jdbcClient.sql("SELECT * FROM clinical_evolutions WHERE appointment_id = :appointmentId")
                .param("appointmentId", appointmentId)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public boolean existsByAppointmentId(UUID appointmentId) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM clinical_evolutions WHERE appointment_id = :appointmentId)")
                        .param("appointmentId", appointmentId)
                        .query(Boolean.class)
                        .single());
    }

    private ClinicalEvolution mapRow(ResultSet rs) throws SQLException {
        return ClinicalEvolution.reconstitute(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("appointment_id")),
                UUID.fromString(rs.getString("psychologist_id")),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
