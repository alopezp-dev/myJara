-- Episodios de urgencias
CREATE TABLE IF NOT EXISTS emergency_episodes (
                                                  id              BIGSERIAL PRIMARY KEY,
                                                  patient_id      BIGINT NOT NULL,
                                                  triage_level    INTEGER NOT NULL,           -- 1=Resucitación, 2=Emergencia, 3=Urgente, 4=Menos urgente, 5=No urgente
                                                  triage_color    VARCHAR(20) NOT NULL,       -- RED, ORANGE, YELLOW, GREEN, BLUE
    chief_complaint VARCHAR(255) NOT NULL,      -- Motivo de consulta
    status          VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    box_id          BIGINT,
    professional_id BIGINT,
    arrival_time    TIMESTAMP NOT NULL DEFAULT NOW(),
    triage_time     TIMESTAMP,
    attention_time  TIMESTAMP,
    discharge_time  TIMESTAMP,
    discharge_type  VARCHAR(30),               -- HOME, ADMISSION, TRANSFER, DEATH, VOLUNTARY
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
    );

-- Boxes de urgencias
CREATE TABLE IF NOT EXISTS emergency_boxes (
                                               id              BIGSERIAL PRIMARY KEY,
                                               name            VARCHAR(50) NOT NULL,       -- Ej: "Box 1", "Reanimación", "Sala de espera"
    type            VARCHAR(30) NOT NULL,       -- GENERAL, RESUSCITATION, OBSERVATION, TRIAGE
    status          VARCHAR(20) NOT NULL DEFAULT 'FREE',  -- FREE, OCCUPIED, CLEANING
    center          VARCHAR(150),
    active          BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Signos vitales del episodio
CREATE TABLE IF NOT EXISTS vital_signs (
                                           id                  BIGSERIAL PRIMARY KEY,
                                           emergency_episode_id BIGINT NOT NULL,
                                           professional_id     BIGINT NOT NULL,
                                           systolic_bp         INTEGER,               -- Tensión sistólica (mmHg)
                                           diastolic_bp        INTEGER,               -- Tensión diastólica (mmHg)
                                           heart_rate          INTEGER,               -- Frecuencia cardiaca (lpm)
                                           respiratory_rate    INTEGER,               -- Frecuencia respiratoria (rpm)
                                           temperature         DECIMAL(4,1),          -- Temperatura (°C)
    oxygen_saturation   INTEGER,               -- Saturación O2 (%)
    pain_scale          INTEGER,               -- Escala de dolor EVA (0-10)
    recorded_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (emergency_episode_id) REFERENCES emergency_episodes(id),
    FOREIGN KEY (professional_id)      REFERENCES professionals(id)
    );

-- Boxes de muestra
INSERT INTO emergency_boxes (name, type, status, center) VALUES
                                                             ('Triaje 1', 'TRIAGE', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Box 1', 'GENERAL', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Box 2', 'GENERAL', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Box 3', 'GENERAL', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Reanimación 1', 'RESUSCITATION', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Observación 1', 'OBSERVATION', 'FREE', 'Hospital San Pedro de Alcántara'),
                                                             ('Observación 2', 'OBSERVATION', 'FREE', 'Hospital San Pedro de Alcántara');

-- Índices
CREATE INDEX idx_emergency_episodes_patient ON emergency_episodes(patient_id);
CREATE INDEX idx_emergency_episodes_status  ON emergency_episodes(status);
CREATE INDEX idx_emergency_episodes_triage  ON emergency_episodes(triage_level);
CREATE INDEX idx_vital_signs_episode        ON vital_signs(emergency_episode_id);