CREATE TABLE IF NOT EXISTS professionals (
                                             id              BIGSERIAL PRIMARY KEY,
                                             first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    specialty       VARCHAR(100),
    license_number  VARCHAR(50) UNIQUE NOT NULL,
    email           VARCHAR(150),
    phone           VARCHAR(20),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS agendas (
                                       id              BIGSERIAL PRIMARY KEY,
                                       professional_id BIGINT NOT NULL,
                                       center          VARCHAR(150) NOT NULL,
    day_of_week     INTEGER NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    slot_minutes    INTEGER NOT NULL DEFAULT 15,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
    );

CREATE TABLE IF NOT EXISTS appointments (
                                            id              BIGSERIAL PRIMARY KEY,
                                            patient_id      BIGINT NOT NULL,
                                            professional_id BIGINT NOT NULL,
                                            agenda_id       BIGINT NOT NULL,
                                            start_time      TIMESTAMP NOT NULL,
                                            end_time        TIMESTAMP NOT NULL,
                                            status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason          VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (agenda_id)       REFERENCES agendas(id)
    );

CREATE INDEX idx_appointments_patient    ON appointments(patient_id);
CREATE INDEX idx_appointments_professional ON appointments(professional_id);
CREATE INDEX idx_appointments_start_time ON appointments(start_time);
CREATE INDEX idx_appointments_status     ON appointments(status);