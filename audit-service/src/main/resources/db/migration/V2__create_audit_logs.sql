CREATE TABLE audit_logs (
                            audit_id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT,
                            action VARCHAR(255),
                            description VARCHAR(255),
                            timestamp TIMESTAMP
);