ALTER TABLE users
    ADD COLUMN spotify_refresh_token VARCHAR(1024),
    ADD COLUMN session_token VARCHAR(512);