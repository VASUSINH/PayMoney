

ALTER TABLE payments
    ADD COLUMN razorpay_payment_link_id VARCHAR(255);

ALTER TABLE payments
    ADD COLUMN razorpay_payment_link_reference_id VARCHAR(255);

CREATE UNIQUE INDEX uq_payments_payment_link_id
    ON payments (razorpay_payment_link_id);

CREATE UNIQUE INDEX uq_payments_payment_link_reference_id
    ON payments (razorpay_payment_link_reference_id);
