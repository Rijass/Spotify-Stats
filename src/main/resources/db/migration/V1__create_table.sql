CREATE TABLE users
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    username           VARCHAR(100)                        NOT NULL UNIQUE,
    email              VARCHAR(150)                        NOT NULL UNIQUE,
    password           VARCHAR(255)                        NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE spotify_tokens
(
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id                 BIGINT NOT NULL UNIQUE,
    refresh_token           VARCHAR(1024),
    access_token            VARCHAR(1024),
    access_token_expires_at TIMESTAMP,
    CONSTRAINT fk_spotify_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);
