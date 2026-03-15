CREATE TABLE patients (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    cpf           CHAR(11)    NOT NULL UNIQUE,
    birth_date    DATE        NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    guardian_id   UUID        REFERENCES patients(id),
    active        BOOLEAN     NOT NULL DEFAULT true,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patients_cpf    ON patients(cpf);
CREATE INDEX idx_patients_email  ON patients(email);
CREATE INDEX idx_patients_active ON patients(active);
