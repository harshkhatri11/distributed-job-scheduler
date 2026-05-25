CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS jobs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    job_type         VARCHAR(50)  NOT NULL,
    cron_expression  VARCHAR(100) NOT NULL,
    job_config       JSONB,
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    max_retries      INTEGER      NOT NULL DEFAULT 3,
    timeout_seconds  INTEGER      NOT NULL DEFAULT 30,
    next_fire_time   TIMESTAMPTZ,
    last_fire_time   TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);