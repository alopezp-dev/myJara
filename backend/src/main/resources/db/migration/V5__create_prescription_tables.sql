-- Catálogo de medicamentos (basado en AEMPS)
CREATE TABLE IF NOT EXISTS medications (
                                           id              BIGSERIAL PRIMARY KEY,
                                           national_code   VARCHAR(20) UNIQUE NOT NULL,  -- Código nacional AEMPS
    name            VARCHAR(255) NOT NULL,
    active_ingredient VARCHAR(255),               -- Principio activo
    pharmaceutical_form VARCHAR(100),             -- Forma farmacéutica
    dosage          VARCHAR(100),                 -- Dosis
    route           VARCHAR(50),                  -- Vía de administración
    requires_prescription BOOLEAN DEFAULT TRUE,
    active          BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Prescripciones (receta electrónica)
CREATE TABLE IF NOT EXISTS prescriptions (
                                             id              BIGSERIAL PRIMARY KEY,
                                             patient_id      BIGINT NOT NULL,
                                             professional_id BIGINT NOT NULL,
                                             encounter_id    BIGINT,
                                             medication_id   BIGINT NOT NULL,
                                             dose            VARCHAR(100) NOT NULL,        -- Ej: "500mg"
    frequency       VARCHAR(100) NOT NULL,        -- Ej: "Cada 8 horas"
    duration        VARCHAR(100),                 -- Ej: "7 días"
    route           VARCHAR(50),                  -- Vía de administración
    instructions    TEXT,                         -- Instrucciones al paciente
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date        DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (encounter_id)    REFERENCES encounters(id),
    FOREIGN KEY (medication_id)   REFERENCES medications(id)
    );

-- Interacciones medicamentosas conocidas
CREATE TABLE IF NOT EXISTS drug_interactions (
                                                 id              BIGSERIAL PRIMARY KEY,
                                                 medication_a_id BIGINT NOT NULL,
                                                 medication_b_id BIGINT NOT NULL,
                                                 severity        VARCHAR(20) NOT NULL DEFAULT 'MODERATE',  -- MILD, MODERATE, SEVERE
    description     TEXT NOT NULL,
    FOREIGN KEY (medication_a_id) REFERENCES medications(id),
    FOREIGN KEY (medication_b_id) REFERENCES medications(id)
    );

-- Índices
CREATE INDEX idx_prescriptions_patient    ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_status     ON prescriptions(status);
CREATE INDEX idx_medications_name         ON medications(name);
CREATE INDEX idx_medications_code         ON medications(national_code);

-- Medicamentos de muestra
INSERT INTO medications (national_code, name, active_ingredient, pharmaceutical_form, dosage, route) VALUES
                                                                                                         ('656789', 'Amoxicilina 500mg cápsulas', 'Amoxicilina', 'Cápsulas', '500mg', 'Oral'),
                                                                                                         ('723451', 'Ibuprofeno 600mg comprimidos', 'Ibuprofeno', 'Comprimidos', '600mg', 'Oral'),
                                                                                                         ('489234', 'Omeprazol 20mg cápsulas', 'Omeprazol', 'Cápsulas', '20mg', 'Oral'),
                                                                                                         ('534127', 'Paracetamol 1g comprimidos', 'Paracetamol', 'Comprimidos', '1g', 'Oral'),
                                                                                                         ('612890', 'Atorvastatina 20mg comprimidos', 'Atorvastatina', 'Comprimidos', '20mg', 'Oral'),
                                                                                                         ('778234', 'Metformina 850mg comprimidos', 'Metformina', 'Comprimidos', '850mg', 'Oral'),
                                                                                                         ('845671', 'Salbutamol 100mcg inhalador', 'Salbutamol', 'Inhalador', '100mcg', 'Inhalatoria'),
                                                                                                         ('923456', 'Enalapril 10mg comprimidos', 'Enalapril', 'Comprimidos', '10mg', 'Oral'),
                                                                                                         ('156789', 'Lorazepam 1mg comprimidos', 'Lorazepam', 'Comprimidos', '1mg', 'Oral'),
                                                                                                         ('267834', 'Azitromicina 500mg comprimidos', 'Azitromicina', 'Comprimidos', '500mg', 'Oral');

-- Interacción de ejemplo: Ibuprofeno + Enalapril
INSERT INTO drug_interactions (medication_a_id, medication_b_id, severity, description)
VALUES (2, 8, 'MODERATE', 'Los AINEs pueden reducir el efecto antihipertensivo de los IECAs y aumentar el riesgo de insuficiencia renal aguda.');