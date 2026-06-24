ALTER TABLE users
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE refresh_token (
                               id BIGSERIAL PRIMARY KEY,
                               token_hash VARCHAR(255) NOT NULL UNIQUE,
                               user_id BIGINT NOT NULL,
                               expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               revoked BOOLEAN NOT NULL DEFAULT FALSE,
                               last_used_at TIMESTAMP WITH TIME ZONE,

                               CONSTRAINT fk_refresh_token_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users (id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_hash
    ON refresh_token (token_hash);

CREATE INDEX idx_refresh_token_user_id
    ON refresh_token (user_id);