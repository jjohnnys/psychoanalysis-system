package com.jjohnnys.psa.api;

import com.jjohnnys.psa.api.dto.request.CreatePsychologistRequest;
import com.jjohnnys.psa.api.dto.response.PsychologistResponse;
import com.jjohnnys.psa.application.usecase.RegisterPsychologistUseCase;
import com.jjohnnys.psa.application.usecase.command.RegisterPsychologistCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/psychologists")
public class PsychologistController {

    private final RegisterPsychologistUseCase registerPsychologistUseCase;

    public PsychologistController(RegisterPsychologistUseCase registerPsychologistUseCase) {
        this.registerPsychologistUseCase = registerPsychologistUseCase;
    }

    @PostMapping
    public ResponseEntity<PsychologistResponse> create(@Valid @RequestBody CreatePsychologistRequest request) {
        var psychologist = registerPsychologistUseCase.execute(new RegisterPsychologistCommand(
                request.name(),
                request.crp(),
                request.specialty(),
                request.baseSessionRate()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(new PsychologistResponse(
                psychologist.id(),
                psychologist.name(),
                psychologist.crp(),
                psychologist.specialty(),
                psychologist.baseSessionRate(),
                psychologist.createdAt(),
                psychologist.updatedAt()
        ));
    }
}
