ALTER TABLE transactions
    ADD COLUMN payment_id BIGINT;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments(payment_id);

CREATE INDEX idx_transactions_payment_id
    ON transactions(payment_id);