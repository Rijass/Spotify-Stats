CREATE TABLE songs
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_track_id VARCHAR(128)                        NOT NULL UNIQUE,
    artist            VARCHAR(255)                        NOT NULL,
    title             VARCHAR(255)                        NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE chart_snapshots
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    chart_key  VARCHAR(100)                        NOT NULL,
    chart_date DATE                                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_chart_key_date UNIQUE (chart_key, chart_date)
);

CREATE TABLE chart_entries
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT      NOT NULL,
    song_id     BIGINT      NOT NULL,
    position    INT         NOT NULL,
    CONSTRAINT fk_chart_entries_snapshot FOREIGN KEY (snapshot_id) REFERENCES chart_snapshots (id) ON DELETE CASCADE,
    CONSTRAINT fk_chart_entries_song FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE,
    CONSTRAINT uq_snapshot_position UNIQUE (snapshot_id, position)
);