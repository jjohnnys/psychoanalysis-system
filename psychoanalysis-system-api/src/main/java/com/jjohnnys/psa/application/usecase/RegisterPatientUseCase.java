package com.jjohnnys.psa.application.usecase;

import com.jjohnnys.psa.application.repository.PatientRepository;
import com.jjohnnys.psa.application.usecase.command.RegisterPatientCommand;
import com.jjohnnys.psa.domain.Patient;
import com.jjohnnys.psa.domain.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPatientUseCase {

    private final PatientRepository patientRepository;

    public RegisterPatientUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient execute(RegisterPatientCommand command) {
        if (patientRepository.existsByCpf(command.cpf())) {
            throw new BusinessException("CPF is already registered");
        }
        if (patientRepository.existsByEmail(command.email())) {
            throw new BusinessException("Email is already registered");
        }

        var patient = Patient.create(
                command.name(),
                command.cpf(),
                command.birthDate(),
                command.email(),
                command.phone(),
                command.guardianId()
        );
        return patientRepository.save(patient);
    }
}
