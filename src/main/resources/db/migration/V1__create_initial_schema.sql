CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL
);

CREATE TABLE wallets (
                         wallet_id BIGSERIAL PRIMARY KEY,
                         balance NUMERIC(19,2) NOT NULL,
                         user_id BIGINT NOT NULL UNIQUE,

                         CONSTRAINT fk_wallet_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(user_id)
);

CREATE TABLE transactions (
                              transaction_id BIGSERIAL PRIMARY KEY,
                              sender_wallet_id BIGINT,
                              receiver_wallet_id BIGINT,
                              amount NUMERIC(19,2) NOT NULL,
                              type VARCHAR(50) NOT NULL,
                              status VARCHAR(50) NOT NULL,
                              created_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_transaction_sender_wallet
                                  FOREIGN KEY (sender_wallet_id)
                                      REFERENCES wallets(wallet_id),

                              CONSTRAINT fk_transaction_receiver_wallet
                                  FOREIGN KEY (receiver_wallet_id)
                                      REFERENCES wallets(wallet_id)
);

CREATE TABLE payments (
                          payment_id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          amount NUMERIC(19,2) NOT NULL,
                          razorpay_order_id VARCHAR(255) NOT NULL UNIQUE,
                          razorpay_payment_id VARCHAR(255),
                          status VARCHAR(50),
                          created_at TIMESTAMP NOT NULL,
                          refunded_amount NUMERIC(19,2),
                          razorpay_refund_id VARCHAR(255),

                          CONSTRAINT fk_payment_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(user_id)
);

CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,
                                  idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                                  user_id BIGINT NOT NULL,
                                  transaction_id BIGINT NOT NULL,
                                  created_at TIMESTAMP NOT NULL
);