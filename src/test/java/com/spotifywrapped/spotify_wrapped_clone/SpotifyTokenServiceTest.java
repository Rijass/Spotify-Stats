package com.spotifywrapped.spotify_wrapped_clone;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.SpotifyTokenDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.service.Security.SensitiveDataService;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.SpotifyTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SpotifyTokenServiceTest {

    private SpotifyTokenService spotifyTokenService;

    @BeforeEach
    void setUp() {
        spotifyTokenService = new SpotifyTokenService(mock(SpotifyTokenDBaccess.class), mock(SensitiveDataService.class));
    }

    @Test
    void calculateAccessTokenExpiryDefaultsToOneHourWhenMissing() {
        Instant now = Instant.now();

        Instant expiresAt = spotifyTokenService.calculateAccessTokenExpiry(null);

        assertTrue(expiresAt.isAfter(now), "Expiry should be in the future");
        assertTrue(expiresAt.isBefore(now.plusSeconds(3600 + 5)), "Expiry should be about one hour ahead");
        assertTrue(expiresAt.isAfter(now.plusSeconds(3600 - 5)), "Expiry should be about one hour ahead");
    }

    @Test
    void calculateAccessTokenExpiryUsesProvidedLifetime() {
        Instant now = Instant.now();

        Instant expiresAt = spotifyTokenService.calculateAccessTokenExpiry(120);

        assertTrue(expiresAt.isAfter(now.plusSeconds(110)), "Expiry should reflect the provided lifetime");
        assertTrue(expiresAt.isBefore(now.plusSeconds(130)), "Expiry should reflect the provided lifetime");
    }

    @Test
    void calculateAccessTokenExpiryIgnoresNonPositiveLifetimes() {
        Instant now = Instant.now();

        Instant expiresAtZero = spotifyTokenService.calculateAccessTokenExpiry(0);
        Instant expiresAtNegative = spotifyTokenService.calculateAccessTokenExpiry(-10);

        assertTrue(expiresAtZero.isAfter(now.plusSeconds(3600 - 5)), "Zero lifetimes should fall back to one hour");
        assertTrue(expiresAtNegative.isAfter(now.plusSeconds(3600 - 5)), "Negative lifetimes should fall back to one hour");
    }
}
