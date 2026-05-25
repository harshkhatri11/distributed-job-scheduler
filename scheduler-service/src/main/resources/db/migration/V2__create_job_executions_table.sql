CREATE TABLE IF NOT EXISTS job_executions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID         NOT NULL REFERENCES jobs(id),
    status          VARCHAR(50)  NOT NULL,
    attempt_number  INTEGER,
    triggered_at    TIMESTAMPTZ,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    result_output   TEXT,
    error_message   TEXT,
    trace_id        VARCHAR(100)
);