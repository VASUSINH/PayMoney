-- An idempotency key only has to be unique for one user, not for everybody.
ALTER TABLE idempotency_keys
    DROP CONSTRAINT IF EXISTS idempotency_keys_idempotency_key_key;

ALTER TABLE idempotency_keys
    ADD CONSTRAINT uq_idempotency_user_key UNIQUE (user_id, idempotency_key);
