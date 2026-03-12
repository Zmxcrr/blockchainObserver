CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_users_email ON users(email);


CREATE TABLE favorite_addresses (
                                    id UUID PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    address VARCHAR(255) NOT NULL,
                                    network VARCHAR(50) NOT NULL,
                                    label VARCHAR(255),
                                    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_favorite_user ON favorite_addresses(user_id);


CREATE TABLE search_history (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                query VARCHAR(255) NOT NULL,
                                network VARCHAR(50) NOT NULL,
                                search_type VARCHAR(50) NOT NULL,
                                created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_history_user ON search_history(user_id);


CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              hash VARCHAR(255) NOT NULL,
                              network VARCHAR(50) NOT NULL,
                              from_address VARCHAR(255) NOT NULL,
                              to_address VARCHAR(255),
                              amount NUMERIC(36, 18) NOT NULL,
                              fee NUMERIC(36, 18) NOT NULL,
                              block_number BIGINT NOT NULL,
                              block_hash VARCHAR(255),
                              timestamp TIMESTAMP NOT NULL,
                              status VARCHAR(50) NOT NULL,
                              contract_address VARCHAR(255)
);

CREATE UNIQUE INDEX idx_tx_hash_network
    ON transactions(hash, network);

CREATE INDEX idx_tx_from
    ON transactions(from_address);

CREATE INDEX idx_tx_to
    ON transactions(to_address);


ALTER TABLE favorite_addresses
    ADD CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE search_history
    ADD CONSTRAINT fk_history_user
        FOREIGN KEY (user_id) REFERENCES users(id);