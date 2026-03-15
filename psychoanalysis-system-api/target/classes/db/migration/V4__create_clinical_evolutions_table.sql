CREATE TABLE clinical_evolutions (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id   UUID      NOT NULL UNIQUE REFERENCES appointments(id),
    psychologist_id  UUID      NOT NULL REFERENCES psychologists(id),
    content          TEXT      NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clinical_evolutions_appointment_id  ON clinical_evolutions(appointment_id);
CREATE INDEX idx_clinical_evolutions_psychologist_id ON clinical_evolutions(psychologist_id);
