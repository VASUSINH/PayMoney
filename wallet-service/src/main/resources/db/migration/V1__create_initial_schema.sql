CREATE TABLE wallets (
                         wallet_id BIGSERIAL PRIMARY KEY,
                         balance NUMERIC(19,2) NOT NULL,
                         user_id BIGINT NOT NULL UNIQUE
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

CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,
                                  idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                                  user_id BIGINT NOT NULL,
                                  transaction_id BIGINT NOT NULL,
                                  created_at TIMESTAMP NOT NULL
);
