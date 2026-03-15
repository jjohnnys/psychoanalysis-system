CREATE TABLE appointments (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID          NOT NULL REFERENCES patients(id),
    psychologist_id  UUID          NOT NULL REFERENCES psychologists(id),
    start_date_time  TIMESTAMP     NOT NULL,
    end_date_time    TIMESTAMP     NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'AGENDADA',
    applied_value    DECIMAL(10,2) NOT NULL CHECK (applied_value >= 0),
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_appointment_times CHECK (end_date_time > start_date_time)
);

CREATE INDEX idx_appointments_patient_id      ON appointments(patient_id);
CREATE INDEX idx_appointments_psychologist_id ON appointments(psychologist_id);
CREATE INDEX idx_appointments_status          ON appointments(status);
CREATE INDEX idx_appointments_start_date_time ON appointments(start_date_time);
