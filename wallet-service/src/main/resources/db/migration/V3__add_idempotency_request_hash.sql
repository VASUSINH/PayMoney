ALTER TABLE idempotency_keys
    ADD COLUMN request_hash VARCHAR(64);