CREATE TABLE payments (
                          payment_id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          amount NUMERIC(19,2) NOT NULL,
                          razorpay_payment_id VARCHAR(255),
                          status VARCHAR(50),
                          created_at TIMESTAMP NOT NULL,
                          refunded_amount NUMERIC(19,2),
                          razorpay_refund_id VARCHAR(255)
);

CREATE UNIQUE INDEX uq_payments_razorpay_payment_id
    ON payments (razorpay_payment_id);
