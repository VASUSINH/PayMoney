
ALTER TABLE idempotency_keys
    ALTER COLUMN idempotency_key TYPE VARCHAR(64);

CREATE UNIQUE INDEX uq_payments_razorpay_payment_id
    ON payments (razorpay_payment_id);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT fk_idempotency_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT fk_idempotency_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES transactions (transaction_id);