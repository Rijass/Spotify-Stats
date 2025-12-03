CREATE TABLE spotify_tokens
(
    id                      BIGINT AUTO_INCREMENT NOT NULL,
    user_id                 BIGINT NOT NULL,
    refresh_token           VARCHAR(1024) NULL,
    access_token            VARCHAR(1024) NULL,
    access_token_expires_at datetime NULL,
    CONSTRAINT pk_spotify_tokens PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    username           VARCHAR(100) NOT NULL,
    email              VARCHAR(150) NOT NULL,
    password           VARCHAR(255) NOT NULL,
    session_token      VARCHAR(512) NULL,
    session_expires_at datetime NULL,
    created_at         datetime     NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE spotify_tokens
    ADD CONSTRAINT uc_spotify_tokens_user UNIQUE (user_id);

ALTER TABLE spotify_tokens
    ADD CONSTRAINT FK_SPOTIFY_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
CREATE TABLE spotify_tokens
(
    id                      BIGINT AUTO_INCREMENT NOT NULL,
    user_id                 BIGINT                NOT NULL,
    refresh_token           VARCHAR(1024)         NULL,
    access_token            VARCHAR(1024)         NULL,
    access_token_expires_at datetime              NULL,
    CONSTRAINT pk_spotify_tokens PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    username           VARCHAR(100)          NOT NULL,
    email              VARCHAR(150)          NOT NULL,
    password           VARCHAR(255)          NOT NULL,
    session_token      VARCHAR(512)          NULL,
    session_expires_at datetime              NULL,
    created_at         datetime              NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE spotify_tokens
    ADD CONSTRAINT uc_spotify_tokens_user UNIQUE (user_id);

ALTER TABLE spotify_tokens
    ADD CONSTRAINT FK_SPOTIFY_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);