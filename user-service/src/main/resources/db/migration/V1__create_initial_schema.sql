CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL
);
