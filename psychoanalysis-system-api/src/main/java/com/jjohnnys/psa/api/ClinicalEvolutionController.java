package com.jjohnnys.psa.api;

import com.jjohnnys.psa.api.dto.request.CreateClinicalEvolutionRequest;
import com.jjohnnys.psa.api.dto.response.ClinicalEvolutionResponse;
import com.jjohnnys.psa.application.usecase.RegisterClinicalEvolutionUseCase;
import com.jjohnnys.psa.application.usecase.command.RegisterClinicalEvolutionCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/clinical-evolutions")
public class ClinicalEvolutionController {

    private final RegisterClinicalEvolutionUseCase registerClinicalEvolutionUseCase;

    public ClinicalEvolutionController(RegisterClinicalEvolutionUseCase registerClinicalEvolutionUseCase) {
        this.registerClinicalEvolutionUseCase = registerClinicalEvolutionUseCase;
    }

    @PostMapping
    public ResponseEntity<ClinicalEvolutionResponse> create(@Valid @RequestBody CreateClinicalEvolutionRequest request) {
        var evolution = registerClinicalEvolutionUseCase.execute(new RegisterClinicalEvolutionCommand(
                request.appointmentId(),
                request.psychologistId(),
                request.content()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(new ClinicalEvolutionResponse(
                evolution.id(),
                evolution.appointmentId(),
                evolution.psychologistId(),
                evolution.content(),
                evolution.createdAt(),
                evolution.updatedAt()
        ));
    }
}
