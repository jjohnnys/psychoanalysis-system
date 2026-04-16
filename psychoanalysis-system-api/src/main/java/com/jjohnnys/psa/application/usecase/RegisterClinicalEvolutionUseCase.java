package com.jjohnnys.psa.application.usecase;

import com.jjohnnys.psa.application.repository.AppointmentRepository;
import com.jjohnnys.psa.application.repository.ClinicalEvolutionRepository;
import com.jjohnnys.psa.application.repository.PsychologistRepository;
import com.jjohnnys.psa.application.usecase.command.RegisterClinicalEvolutionCommand;
import com.jjohnnys.psa.domain.ClinicalEvolution;
import com.jjohnnys.psa.domain.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterClinicalEvolutionUseCase {

    private final ClinicalEvolutionRepository clinicalEvolutionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PsychologistRepository psychologistRepository;

    public RegisterClinicalEvolutionUseCase(ClinicalEvolutionRepository clinicalEvolutionRepository,
                                            AppointmentRepository appointmentRepository,
                                            PsychologistRepository psychologistRepository) {
        this.clinicalEvolutionRepository = clinicalEvolutionRepository;
        this.appointmentRepository = appointmentRepository;
        this.psychologistRepository = psychologistRepository;
    }

    @Transactional
    public ClinicalEvolution execute(RegisterClinicalEvolutionCommand command) {
        var appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new BusinessException("Appointment not found"));

        psychologistRepository.findById(command.psychologistId())
                .orElseThrow(() -> new BusinessException("Psychologist not found"));

        if (!appointment.psychologistId().equals(command.psychologistId())) {
            throw new BusinessException("Only the linked psychologist can register the clinical evolution");
        }

        if (clinicalEvolutionRepository.existsByAppointmentId(command.appointmentId())) {
            throw new BusinessException("Clinical evolution already exists for this appointment");
        }

        var evolution = ClinicalEvolution.create(
                command.appointmentId(),
                command.psychologistId(),
                command.content()
        );

        return clinicalEvolutionRepository.save(evolution);
    }
}
