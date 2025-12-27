package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.SpotifyTokenDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.SpotifyToken;
import com.spotifywrapped.spotify_wrapped_clone.service.Security.SensitiveDataService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SpotifyTokenService {

    private static final int ACCESS_TOKEN_LIFETIME_SECONDS = 3600;

    private final SpotifyTokenDBaccess spotifyTokenDBaccess;
    private final SensitiveDataService sensitiveDataService;

    public SpotifyTokenService(SpotifyTokenDBaccess spotifyTokenDBaccess, SensitiveDataService sensitiveDataService) {
        this.spotifyTokenDBaccess = spotifyTokenDBaccess;
        this.sensitiveDataService = sensitiveDataService;
    }

    public DecryptedSpotifyTokens getDecryptedTokens(Long userId) {
        SpotifyToken token = spotifyTokenDBaccess.findByUserId(userId);
        if (token == null) {
            return null;
        }

        return new DecryptedSpotifyTokens(
                sensitiveDataService.decrypt(token.getRefreshToken()),
                sensitiveDataService.decrypt(token.getAccessToken()),
                token.getAccessTokenExpiresAt()
        );
    }

    public boolean hasRefreshToken(Long userId) {
        SpotifyToken token = spotifyTokenDBaccess.findByUserId(userId);
        return token != null && token.getRefreshToken() != null && !token.getRefreshToken().isBlank();
    }

    public void updateTokens(Long userId, String refreshToken, String accessToken, Instant accessTokenExpiresAt) {
        if (accessToken == null || accessToken.isBlank() || accessTokenExpiresAt == null) {
            return;
        }

        String encryptedRefresh = refreshToken == null || refreshToken.isBlank()
                ? null
                : sensitiveDataService.encrypt(refreshToken);

        spotifyTokenDBaccess.createOrUpdateToken(
                userId,
                encryptedRefresh,
                sensitiveDataService.encrypt(accessToken),
                accessTokenExpiresAt
        );
    }

    public Instant calculateAccessTokenExpiry(Integer expiresInSeconds) {
        int lifetime = (expiresInSeconds != null && expiresInSeconds > 0)
                ? expiresInSeconds
                : ACCESS_TOKEN_LIFETIME_SECONDS;

        return Instant.now().plusSeconds(lifetime);
    }

    public record DecryptedSpotifyTokens(String refreshToken, String accessToken, Instant accessTokenExpiresAt) { }
}
