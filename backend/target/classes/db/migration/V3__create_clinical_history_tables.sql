-- Episodios clínicos (cada visita o ingreso genera un episodio)
CREATE TABLE IF NOT EXISTS encounters (
                                          id              BIGSERIAL PRIMARY KEY,
                                          patient_id      BIGINT NOT NULL,
                                          professional_id BIGINT NOT NULL,
                                          appointment_id  BIGINT,
                                          type            VARCHAR(20) NOT NULL DEFAULT 'OUTPATIENT',
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    start_date      TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date        TIMESTAMP,
    reason          VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (appointment_id)  REFERENCES appointments(id)
    );

-- Diagnósticos con codificación CIE-10
CREATE TABLE IF NOT EXISTS conditions (
                                          id              BIGSERIAL PRIMARY KEY,
                                          encounter_id    BIGINT NOT NULL,
                                          patient_id      BIGINT NOT NULL,
                                          cie10_code      VARCHAR(10) NOT NULL,
    cie10_desc      VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    onset_date      DATE,
    resolved_date   DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (encounter_id) REFERENCES encounters(id),
    FOREIGN KEY (patient_id)   REFERENCES patients(id)
    );

-- Alergias del paciente
CREATE TABLE IF NOT EXISTS allergies (
                                         id              BIGSERIAL PRIMARY KEY,
                                         patient_id      BIGINT NOT NULL UNIQUE,
                                         substance       VARCHAR(150) NOT NULL,
    reaction        VARCHAR(255),
    severity        VARCHAR(20) NOT NULL DEFAULT 'MODERATE',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    onset_date      DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
    );

-- Notas clínicas del episodio
CREATE TABLE IF NOT EXISTS clinical_notes (
                                              id              BIGSERIAL PRIMARY KEY,
                                              encounter_id    BIGINT NOT NULL,
                                              professional_id BIGINT NOT NULL,
                                              type            VARCHAR(30) NOT NULL DEFAULT 'EVOLUTION',
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (encounter_id)    REFERENCES encounters(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
    );

-- Catálogo CIE-10 (subset para búsquedas)
CREATE TABLE IF NOT EXISTS cie10_catalog (
                                             code            VARCHAR(10) PRIMARY KEY,
    description     VARCHAR(255) NOT NULL,
    category        VARCHAR(100)
    );

CREATE INDEX idx_encounters_patient    ON encounters(patient_id);
CREATE INDEX idx_encounters_status     ON encounters(status);
CREATE INDEX idx_conditions_patient    ON conditions(patient_id);
CREATE INDEX idx_conditions_cie10      ON conditions(cie10_code);
CREATE INDEX idx_cie10_description     ON cie10_catalog(description);