CREATE TABLE IF NOT EXISTS idempotency_records (
    idempotency_key UUID PRIMARY KEY,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
