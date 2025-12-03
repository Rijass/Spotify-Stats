package com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "spotify_refresh_token", length = 1024)
    private String spotifyRefreshToken;

    @Column(name = "spotify_access_token", length = 1024)
    private String spotifyAccessToken;

    @Column(name = "spotify_access_token_expires_at")
    private Instant spotifyAccessTokenExpiresAt;

    @Column(name = "session_token", length = 512)
    private String sessionToken;

    @Column(name = "session_expires_at")
    private Instant sessionExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSpotifyRefreshToken() {
        return spotifyRefreshToken;
    }

    public void setSpotifyRefreshToken(String spotifyRefreshToken) {
        this.spotifyRefreshToken = spotifyRefreshToken;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public Instant getSpotifyAccessTokenExpiresAt() {
        return spotifyAccessTokenExpiresAt;
    }

    public void setSpotifyAccessTokenExpiresAt(Instant spotifyAccessTokenExpiresAt) {
        this.spotifyAccessTokenExpiresAt = spotifyAccessTokenExpiresAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Instant getSessionExpiresAt() {
        return sessionExpiresAt;
    }

    public void setSessionExpiresAt(Instant sessionExpiresAt) {
        this.sessionExpiresAt = sessionExpiresAt;
    }
}