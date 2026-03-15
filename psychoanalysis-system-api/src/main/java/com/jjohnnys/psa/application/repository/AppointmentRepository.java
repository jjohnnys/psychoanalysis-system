package com.jjohnnys.psa.application.repository;

import com.jjohnnys.psa.domain.Appointment;
import com.jjohnnys.psa.domain.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Appointment update(Appointment appointment);

    Optional<Appointment> findById(UUID id);

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByPsychologistId(UUID psychologistId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findOverlappingForPsychologist(UUID psychologistId,
                                                      LocalDateTime startDateTime,
                                                      LocalDateTime endDateTime);

    boolean hasOverlap(UUID psychologistId, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, UUID excludeAppointmentId);

    boolean existsByPatientId(UUID patientId);
}
