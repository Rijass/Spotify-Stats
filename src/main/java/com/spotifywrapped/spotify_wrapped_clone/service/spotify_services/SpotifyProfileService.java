package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyProfileDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class SpotifyProfileService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyTokenService spotifyTokenService;

    @Value("${spring.security.oauth2.client.provider.spotify.user-info-uri}")
    private String userInfoUri;

    public SpotifyProfileService(
            SpotifyAuthService spotifyAuthService,
            SpotifyTokenService spotifyTokenService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyTokenService = spotifyTokenService;
    }

    /**
     * Lädt das Spotify-Profil eines Nutzers anhand seines gespeicherten Tokens.
     * – Holt automatisch einen frischen Access-Token falls nötig.
     */
    public SpotifyProfileDto fetchProfile(User user) {
        if (user == null) {
            return null;
        }

        SpotifyTokenService.DecryptedSpotifyTokens tokens = spotifyTokenService.getDecryptedTokens(user.getId());
        if (tokens == null || tokens.refreshToken() == null || tokens.refreshToken().isBlank()) {
            return null;
        }

        // Stellt sicher, dass wir einen gültigen Access Token haben
        String accessToken = getValidAccessToken(user.getId(), tokens);

        // Anfrage an Spotify /me Endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<SpotifyUserProfileResponse> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SpotifyUserProfileResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to load Spotify profile with status: " + response.getStatusCode());
        }

        SpotifyUserProfileResponse profile = response.getBody();

        // Erstes Profilbild verwenden (Spotify gibt meist nur eins zurück)
        String imageUrl = (profile.images() != null && !profile.images().isEmpty())
                ? profile.images().get(0).url()
                : null;

        return new SpotifyProfileDto(
                profile.displayName(),
                profile.followers().total(),
                imageUrl
        );
    }

    /**
     * Liefert einen gültigen Access Token:
     * – Falls der gespeicherte Token noch gültig ist → wird er genutzt.
     * – Falls er abgelaufen ist → wird ein neuer per Refresh Token geholt.
     * – Speichert neue Token verschlüsselt in der Datenbank.
     */
    private String getValidAccessToken(Long userId, SpotifyTokenService.DecryptedSpotifyTokens tokens) {
        Instant now = Instant.now();

        if (tokens.accessToken() != null &&
                tokens.accessTokenExpiresAt() != null &&
                tokens.accessTokenExpiresAt().isAfter(now)) {
            return tokens.accessToken();
        }

        // Token ist abgelaufen → neuen holen
        SpotifyAuthService.SpotifyTokenResponse tokenResponse =
                spotifyAuthService.refreshAccessToken(tokens.refreshToken());

        Instant expiresAt = spotifyTokenService.calculateAccessTokenExpiry(
                tokenResponse.expiresIn(),
                now
        );

        // Tokens sicher in der DB aktualisieren
        spotifyTokenService.updateTokens(
                userId,
                tokenResponse.refreshToken(),
                tokenResponse.accessToken(),
                expiresAt
        );

        return tokenResponse.accessToken();
    }

    /**
     * Response DTOs für das Spotify User-Profil.
     */
    public record SpotifyUserProfileResponse(
            @JsonProperty("display_name") String displayName,
            SpotifyFollowers followers,
            List<SpotifyImage> images
    ) {}

    public record SpotifyFollowers(Integer total) {}

    public record SpotifyImage(String url, Integer height, Integer width) {}
}

