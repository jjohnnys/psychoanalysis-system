package com.jjohnnys.psa.application.repository;

import com.jjohnnys.psa.domain.Psychologist;
import com.jjohnnys.psa.domain.Specialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PsychologistRepository {

    Psychologist save(Psychologist psychologist);

    Psychologist update(Psychologist psychologist);

    Optional<Psychologist> findById(UUID id);

    Optional<Psychologist> findByCrp(String crp);

    List<Psychologist> findAll();

    List<Psychologist> findBySpecialty(Specialty specialty);

    boolean existsByCrp(String crp);
}
