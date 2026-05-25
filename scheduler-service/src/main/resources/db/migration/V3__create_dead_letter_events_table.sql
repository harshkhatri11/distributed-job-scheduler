CREATE TABLE IF NOT EXISTS dead_letter_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID         NOT NULL,
    execution_id    UUID,
    payload         TEXT,
    failure_reason  TEXT,
    attempts_made   INTEGER,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);