package com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "chart_entries", uniqueConstraints = {
        @UniqueConstraint(name = "uq_snapshot_position", columnNames = {"snapshot_id", "position"})
})
public class ChartEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private ChartSnapshot snapshot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "position", nullable = false)
    private Integer position;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChartSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ChartSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
