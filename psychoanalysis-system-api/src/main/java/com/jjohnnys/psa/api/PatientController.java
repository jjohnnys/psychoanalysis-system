package com.jjohnnys.psa.api;

import com.jjohnnys.psa.api.dto.request.CreatePatientRequest;
import com.jjohnnys.psa.api.dto.response.PatientResponse;
import com.jjohnnys.psa.application.usecase.RegisterPatientUseCase;
import com.jjohnnys.psa.application.usecase.command.RegisterPatientCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/patients")
public class PatientController {

    private final RegisterPatientUseCase registerPatientUseCase;

    public PatientController(RegisterPatientUseCase registerPatientUseCase) {
        this.registerPatientUseCase = registerPatientUseCase;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        var patient = registerPatientUseCase.execute(new RegisterPatientCommand(
                request.name(),
                request.cpf(),
                request.birthDate(),
                request.email(),
                request.phone(),
                request.guardianId()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(new PatientResponse(
                patient.id(),
                patient.name(),
                patient.cpf(),
                patient.birthDate(),
                patient.email(),
                patient.phone(),
                patient.guardianId(),
                patient.active(),
                patient.createdAt(),
                patient.updatedAt()
        ));
    }
}
