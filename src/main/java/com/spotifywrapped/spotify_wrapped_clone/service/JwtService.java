package com.spotifywrapped.spotify_wrapped_clone.service;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_PURPOSE = "purpose";
    private static final String PURPOSE_SPOTIFY_STATE = "spotify_state";

    private final SecretKey secretKey;
    private final Duration accessTokenExpiry;
    private final Duration spotifyStateExpiry;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") Duration accessTokenExpiry,
            @Value("${app.jwt.spotify-state-expiration:10m}") Duration spotifyStateExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.spotifyStateExpiry = spotifyStateExpiry;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpiry)))
                .signWith(secretKey)
                .compact();
    }

    public String generateSpotifyStateToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_PURPOSE, PURPOSE_SPOTIFY_STATE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(spotifyStateExpiry)))
                .signWith(secretKey)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long parseSpotifyStateUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }

        if (!PURPOSE_SPOTIFY_STATE.equals(claims.get(CLAIM_PURPOSE))) {
            return null;
        }

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}