package com.jjohnnys.psa.application.repository;

import com.jjohnnys.psa.domain.ClinicalEvolution;

import java.util.Optional;
import java.util.UUID;

public interface ClinicalEvolutionRepository {

    ClinicalEvolution save(ClinicalEvolution evolution);

    ClinicalEvolution update(ClinicalEvolution evolution);

    Optional<ClinicalEvolution> findById(UUID id);

    Optional<ClinicalEvolution> findByAppointmentId(UUID appointmentId);

    boolean existsByAppointmentId(UUID appointmentId);
}
