
ALTER TABLE idempotency_keys
    ALTER COLUMN idempotency_key TYPE VARCHAR(64);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT fk_idempotency_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES transactions (transaction_id);
