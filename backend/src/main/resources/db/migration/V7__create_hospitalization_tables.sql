-- Unidades de hospitalización
CREATE TABLE IF NOT EXISTS hospital_units (
                                              id          BIGSERIAL PRIMARY KEY,
                                              name        VARCHAR(100) NOT NULL,       -- Ej: "Medicina Interna", "Cirugía"
    floor       VARCHAR(10),                 -- Planta
    beds_total  INTEGER NOT NULL DEFAULT 0,
    center      VARCHAR(150),
    active      BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Camas
CREATE TABLE IF NOT EXISTS hospital_beds (
                                             id          BIGSERIAL PRIMARY KEY,
                                             unit_id     BIGINT NOT NULL,
                                             bed_code    VARCHAR(20) NOT NULL UNIQUE, -- Ej: "101A", "202B"
    status      VARCHAR(20) NOT NULL DEFAULT 'FREE', -- FREE, OCCUPIED, CLEANING, RESERVED
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (unit_id) REFERENCES hospital_units(id)
    );

-- Ingresos hospitalarios
CREATE TABLE IF NOT EXISTS admissions (
                                          id                  BIGSERIAL PRIMARY KEY,
                                          patient_id          BIGINT NOT NULL,
                                          bed_id              BIGINT NOT NULL,
                                          professional_id     BIGINT NOT NULL,
                                          emergency_episode_id BIGINT,
                                          admission_type      VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, EMERGENCY, TRANSFER
    reason              TEXT NOT NULL,
    diagnosis           VARCHAR(255),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DISCHARGED, TRANSFERRED
    admission_date      TIMESTAMP NOT NULL DEFAULT NOW(),
    discharge_date      TIMESTAMP,
    discharge_notes     TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)            REFERENCES patients(id),
    FOREIGN KEY (bed_id)                REFERENCES hospital_beds(id),
    FOREIGN KEY (professional_id)       REFERENCES professionals(id),
    FOREIGN KEY (emergency_episode_id)  REFERENCES emergency_episodes(id)
    );

-- Evoluciones clínicas del ingreso
CREATE TABLE IF NOT EXISTS admission_notes (
                                               id              BIGSERIAL PRIMARY KEY,
                                               admission_id    BIGINT NOT NULL,
                                               professional_id BIGINT NOT NULL,
                                               note_type       VARCHAR(30) NOT NULL DEFAULT 'EVOLUTION', -- EVOLUTION, ORDER, NURSING, DISCHARGE
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (admission_id)    REFERENCES admissions(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
    );

-- Índices
CREATE INDEX idx_admissions_patient ON admissions(patient_id);
CREATE INDEX idx_admissions_status  ON admissions(status);
CREATE INDEX idx_hospital_beds_unit ON hospital_beds(unit_id);
CREATE INDEX idx_admission_notes    ON admission_notes(admission_id);

-- Unidades de muestra
INSERT INTO hospital_units (name, floor, beds_total, center) VALUES
                                                                 ('Medicina Interna', '2', 30, 'Hospital San Pedro de Alcántara'),
                                                                 ('Cirugía General', '3', 20, 'Hospital San Pedro de Alcántara'),
                                                                 ('Cardiología', '4', 15, 'Hospital San Pedro de Alcántara'),
                                                                 ('Pediatría', '5', 10, 'Hospital San Pedro de Alcántara'),
                                                                 ('UCI', '1', 8, 'Hospital San Pedro de Alcántara');

-- Camas de muestra (Medicina Interna)
INSERT INTO hospital_beds (unit_id, bed_code, status) VALUES
                                                          (1, '201A', 'FREE'), (1, '201B', 'FREE'),
                                                          (1, '202A', 'FREE'), (1, '202B', 'FREE'),
                                                          (1, '203A', 'FREE'), (1, '203B', 'FREE');

-- Camas de muestra (UCI)
INSERT INTO hospital_beds (unit_id, bed_code, status) VALUES
                                                          (5, 'UCI-1', 'FREE'), (5, 'UCI-2', 'FREE'),
                                                          (5, 'UCI-3', 'FREE'), (5, 'UCI-4', 'FREE');