package com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "songs", uniqueConstraints = {
        @UniqueConstraint(name = "uq_song_provider_track", columnNames = "provider_track_id")
})
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "provider_track_id", nullable = false, length = 128)
    private String providerTrackId;

    @Column(name = "artist", nullable = false, length = 255)
    private String artist;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderTrackId() {
        return providerTrackId;
    }

    public void setProviderTrackId(String providerTrackId) {
        this.providerTrackId = providerTrackId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
