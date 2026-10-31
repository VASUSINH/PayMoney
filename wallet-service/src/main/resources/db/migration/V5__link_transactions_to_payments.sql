ALTER TABLE transactions
    ADD COLUMN payment_id BIGINT;

CREATE INDEX idx_transactions_payment_id
    ON transactions(payment_id);
