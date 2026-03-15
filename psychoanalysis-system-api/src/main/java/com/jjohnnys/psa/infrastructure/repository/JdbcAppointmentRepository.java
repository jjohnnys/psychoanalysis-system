package com.jjohnnys.psa.infrastructure.repository;

import com.jjohnnys.psa.application.repository.AppointmentRepository;
import com.jjohnnys.psa.domain.Appointment;
import com.jjohnnys.psa.domain.AppointmentStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAppointmentRepository implements AppointmentRepository {

    private final JdbcClient jdbcClient;

    public JdbcAppointmentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Appointment save(Appointment appointment) {
        jdbcClient.sql("""
                INSERT INTO appointments (id, patient_id, psychologist_id, start_date_time, end_date_time,
                                          status, applied_value, created_at, updated_at)
                VALUES (:id, :patientId, :psychologistId, :startDateTime, :endDateTime,
                        :status, :appliedValue, :createdAt, :updatedAt)
                """)
                .param("id", appointment.id())
                .param("patientId", appointment.patientId())
                .param("psychologistId", appointment.psychologistId())
                .param("startDateTime", appointment.startDateTime())
                .param("endDateTime", appointment.endDateTime())
                .param("status", appointment.status().name())
                .param("appliedValue", appointment.appliedValue())
                .param("createdAt", appointment.createdAt())
                .param("updatedAt", appointment.updatedAt())
                .update();
        return appointment;
    }

    @Override
    public Appointment update(Appointment appointment) {
        jdbcClient.sql("""
                UPDATE appointments
                SET status = :status, applied_value = :appliedValue,
                    start_date_time = :startDateTime, end_date_time = :endDateTime,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", appointment.id())
                .param("status", appointment.status().name())
                .param("appliedValue", appointment.appliedValue())
                .param("startDateTime", appointment.startDateTime())
                .param("endDateTime", appointment.endDateTime())
                .param("updatedAt", appointment.updatedAt())
                .update();
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return jdbcClient.sql("SELECT * FROM appointments WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Override
    public List<Appointment> findByPatientId(UUID patientId) {
        return jdbcClient.sql("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY start_date_time DESC")
                .param("patientId", patientId)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public List<Appointment> findByPsychologistId(UUID psychologistId) {
        return jdbcClient.sql("""
                SELECT * FROM appointments WHERE psychologist_id = :psychologistId
                ORDER BY start_date_time DESC
                """)
                .param("psychologistId", psychologistId)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return jdbcClient.sql("SELECT * FROM appointments WHERE status = :status ORDER BY start_date_time")
                .param("status", status.name())
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public List<Appointment> findOverlappingForPsychologist(UUID psychologistId,
                                                             LocalDateTime startDateTime,
                                                             LocalDateTime endDateTime) {
        return jdbcClient.sql("""
                SELECT * FROM appointments
                WHERE psychologist_id = :psychologistId
                  AND status = 'AGENDADA'
                  AND start_date_time < :endDateTime
                  AND end_date_time > :startDateTime
                """)
                .param("psychologistId", psychologistId)
                .param("startDateTime", startDateTime)
                .param("endDateTime", endDateTime)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public boolean hasOverlap(UUID psychologistId, LocalDateTime startDateTime,
                               LocalDateTime endDateTime, UUID excludeAppointmentId) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("""
                        SELECT EXISTS(
                            SELECT 1 FROM appointments
                            WHERE psychologist_id = :psychologistId
                              AND status = 'AGENDADA'
                              AND start_date_time < :endDateTime
                              AND end_date_time > :startDateTime
                              AND id != :excludeId
                        )
                        """)
                        .param("psychologistId", psychologistId)
                        .param("startDateTime", startDateTime)
                        .param("endDateTime", endDateTime)
                        .param("excludeId", excludeAppointmentId != null ? excludeAppointmentId : UUID.randomUUID())
                        .query(Boolean.class)
                        .single());
    }

    @Override
    public boolean existsByPatientId(UUID patientId) {
        return Boolean.TRUE.equals(
                jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM appointments WHERE patient_id = :patientId)")
                        .param("patientId", patientId)
                        .query(Boolean.class)
                        .single());
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        return Appointment.reconstitute(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("patient_id")),
                UUID.fromString(rs.getString("psychologist_id")),
                rs.getTimestamp("start_date_time").toLocalDateTime(),
                rs.getTimestamp("end_date_time").toLocalDateTime(),
                AppointmentStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("applied_value"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
