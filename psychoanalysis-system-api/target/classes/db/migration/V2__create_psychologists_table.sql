CREATE TABLE psychologists (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name               VARCHAR(255)  NOT NULL,
    crp                VARCHAR(20)   NOT NULL UNIQUE,
    specialty          VARCHAR(50)   NOT NULL,
    base_session_rate  DECIMAL(10,2) NOT NULL CHECK (base_session_rate >= 0),
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_psychologists_crp       ON psychologists(crp);
CREATE INDEX idx_psychologists_specialty ON psychologists(specialty);
