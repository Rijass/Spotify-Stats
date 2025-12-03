ALTER TABLE users
    ADD COLUMN spotify_access_token VARCHAR(1024),
    ADD COLUMN spotify_access_token_expires_at TIMESTAMP;