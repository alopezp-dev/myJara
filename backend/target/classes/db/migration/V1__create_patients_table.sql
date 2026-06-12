CREATE TABLE IF NOT EXISTS patients (
                                        id              BIGSERIAL PRIMARY KEY,
                                        health_card     VARCHAR(20)  UNIQUE NOT NULL,
    dni             VARCHAR(15)  UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    gender          VARCHAR(10),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(255),
    municipality    VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX idx_patients_health_card ON patients(health_card);
CREATE INDEX idx_patients_dni ON patients(dni);
CREATE INDEX idx_patients_last_name ON patients(last_name);