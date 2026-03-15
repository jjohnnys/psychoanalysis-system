package com.jjohnnys.psa.application.repository;

import com.jjohnnys.psa.domain.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {

    Patient save(Patient patient);

    Patient update(Patient patient);

    Optional<Patient> findById(UUID id);

    Optional<Patient> findByCpf(String cpf);

    Optional<Patient> findByEmail(String email);

    List<Patient> findAll();

    List<Patient> findAllActive();

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean hasAppointments(UUID patientId);
}
