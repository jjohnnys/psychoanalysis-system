package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.application.repository.PsychologistRepository;
import com.jjohnnys.psa.domain.Psychologist;
import com.jjohnnys.psa.domain.Specialty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPsychologistRepository implements PsychologistRepository {

    private final JdbcClient jdbcClient;

    public JdbcPsychologistRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Psychologist save(Psychologist psychologist) {
        jdbcClient.sql("""
                INSERT INTO psychologists (id, name, crp, specialty, base_session_rate, created_at, updated_at)
                VALUES (:id, :name, :crp, :specialty, :baseSessionRate, :createdAt, :updatedAt)
                """)
                .param("id", psychologist.id())
                .param("name", psychologist.name())
                .param("crp", psychologist.crp())
                .param("specialty", psychologist.specialty().name())
                .param("baseSessionRate", psychologist.baseSessionRate())
                .param("createdAt", psychologist.createdAt())
                .param("updatedAt", psychologist.updatedAt())
                .update();
        return psychologist;
    }

    @Override
    public Psychologist update(Psychologist psychologist) {
        jdbcClient.sql("""
                UPDATE psychologists
                SET name = :name, specialty = :specialty,
                    base_session_rate = :baseSessionRate, updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", psychologist.id())
                .param("name", psychologist.name())
                .param("specialty", psychologist.specialty().name())
                .param("baseSessionRate", psychologist.baseSessionRate())
                .param("updatedAt", psychologist.updatedAt())
                .update();
        return psychologist;
    }

    @Override
    public Optional<Psychologist> findById(UUID id) {
        return jdbcClient.sql("SELECT * FROM psychologists WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public Optional<Psychologist> findByCrp(String crp) {
        return jdbcClient.sql("SELECT * FROM psychologists WHERE crp = :crp")
                .param("crp", crp)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public List<Psychologist> findAll() {
        return jdbcClient.sql("SELECT * FROM psychologists ORDER BY name")
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public List<Psychologist> findBySpecialty(Specialty specialty) {
        return jdbcClient.sql("SELECT * FROM psychologists WHERE specialty = :specialty ORDER BY name")
                .param("specialty", specialty.name())
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public boolean existsByCrp(String crp) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM psychologists WHERE crp = :crp)")
                        .param("crp", crp)
                        .query(Boolean.class)
                        .single());
    }

    private Psychologist mapRow(ResultSet rs) throws SQLException {
        return Psychologist.reconstitute(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("crp"),
                Specialty.valueOf(rs.getString("specialty")),
                rs.getBigDecimal("base_session_rate"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
