package com.jjohnnys.psa.api;

import com.jjohnnys.psa.api.dto.request.CreateAppointmentRequest;
import com.jjohnnys.psa.api.dto.response.AppointmentResponse;
import com.jjohnnys.psa.application.usecase.ScheduleAppointmentUseCase;
import com.jjohnnys.psa.application.usecase.command.ScheduleAppointmentCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/appointments")
public class AppointmentController {

    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;

    public AppointmentController(ScheduleAppointmentUseCase scheduleAppointmentUseCase) {
        this.scheduleAppointmentUseCase = scheduleAppointmentUseCase;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        var appointment = scheduleAppointmentUseCase.execute(new ScheduleAppointmentCommand(
                request.patientId(),
                request.psychologistId(),
                request.startDateTime(),
                request.endDateTime(),
                request.appliedValue()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(new AppointmentResponse(
                appointment.id(),
                appointment.patientId(),
                appointment.psychologistId(),
                appointment.startDateTime(),
                appointment.endDateTime(),
                appointment.status(),
                appointment.appliedValue(),
                appointment.createdAt(),
                appointment.updatedAt()
        ));
    }
}
