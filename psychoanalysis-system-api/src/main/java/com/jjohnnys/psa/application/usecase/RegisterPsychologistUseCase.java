package com.jjohnnys.psa.application.usecase;

import com.jjohnnys.psa.application.repository.PsychologistRepository;
import com.jjohnnys.psa.application.usecase.command.RegisterPsychologistCommand;
import com.jjohnnys.psa.domain.Psychologist;
import com.jjohnnys.psa.domain.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPsychologistUseCase {

    private final PsychologistRepository psychologistRepository;

    public RegisterPsychologistUseCase(PsychologistRepository psychologistRepository) {
        this.psychologistRepository = psychologistRepository;
    }

    @Transactional
    public Psychologist execute(RegisterPsychologistCommand command) {
        if (psychologistRepository.existsByCrp(command.crp())) {
            throw new BusinessException("CRP is already registered");
        }

        var psychologist = Psychologist.create(
                command.name(),
                command.crp(),
                command.specialty(),
                command.baseSessionRate()
        );
        return psychologistRepository.save(psychologist);
    }
}
