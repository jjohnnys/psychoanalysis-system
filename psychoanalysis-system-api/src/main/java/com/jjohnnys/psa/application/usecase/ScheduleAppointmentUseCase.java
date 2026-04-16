package com.jjohnnys.psa.application.usecase;

import com.jjohnnys.psa.application.repository.AppointmentRepository;
import com.jjohnnys.psa.application.repository.PatientRepository;
import com.jjohnnys.psa.application.repository.PsychologistRepository;
import com.jjohnnys.psa.application.usecase.command.ScheduleAppointmentCommand;
import com.jjohnnys.psa.domain.Appointment;
import com.jjohnnys.psa.domain.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PsychologistRepository psychologistRepository;

    public ScheduleAppointmentUseCase(AppointmentRepository appointmentRepository,
                                      PatientRepository patientRepository,
                                      PsychologistRepository psychologistRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.psychologistRepository = psychologistRepository;
    }

    @Transactional
    public Appointment execute(ScheduleAppointmentCommand command) {
        var patient = patientRepository.findById(command.patientId())
                .orElseThrow(() -> new BusinessException("Patient not found"));

        if (!patient.active()) {
            throw new BusinessException("Cannot schedule appointment for inactive patient");
        }

        psychologistRepository.findById(command.psychologistId())
                .orElseThrow(() -> new BusinessException("Psychologist not found"));

        var appointment = command.endDateTime() == null
                ? Appointment.schedule(
                command.patientId(),
                command.psychologistId(),
                command.startDateTime(),
                command.appliedValue())
                : Appointment.schedule(
                command.patientId(),
                command.psychologistId(),
                command.startDateTime(),
                command.endDateTime(),
                command.appliedValue());

        if (appointmentRepository.hasOverlap(
                appointment.psychologistId(),
                appointment.startDateTime(),
                appointment.endDateTime(),
                null)) {
            throw new BusinessException("Psychologist already has an overlapping appointment");
        }

        return appointmentRepository.save(appointment);
    }
}
