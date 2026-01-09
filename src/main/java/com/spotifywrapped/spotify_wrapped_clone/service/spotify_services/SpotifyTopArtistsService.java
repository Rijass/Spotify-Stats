package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyTopArtistDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class SpotifyTopArtistsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyTokenService spotifyTokenService;

    @Value("https://api.spotify.com/v1/me/top/artists?limit=10&time_range=medium_term")
    private String topArtistsUri;

    public SpotifyTopArtistsService(
            SpotifyAuthService spotifyAuthService,
            SpotifyTokenService spotifyTokenService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyTokenService = spotifyTokenService;
    }

    public List<SpotifyTopArtistDto> fetchTopArtists(User user) {
        if (user == null) return List.of();

        SpotifyTokenService.DecryptedSpotifyTokens tokens =
                spotifyTokenService.getDecryptedTokens(user.getId());

        if (tokens == null || tokens.refreshToken() == null || tokens.refreshToken().isBlank()) {
            return List.of();
        }

        String accessToken = getValidAccessToken(user.getId(), tokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<SpotifyTopArtistsResponse> response =
                restTemplate.exchange(
                        topArtistsUri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        SpotifyTopArtistsResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to load top artists: " + response.getStatusCode());
        }

        return response.getBody().items().stream()
                .map(artist -> new SpotifyTopArtistDto(
                        artist.name(),
                        artist.genres(),
                        artist.images() != null && !artist.images().isEmpty()
                                ? artist.images().getFirst().url()
                                : null,
                        artist.followers().total()
                ))
                .toList();
    }

    private String getValidAccessToken(Long userId, SpotifyTokenService.DecryptedSpotifyTokens tokens) {
        Instant now = Instant.now();

        if (tokens.accessToken() != null &&
                tokens.accessTokenExpiresAt() != null &&
                tokens.accessTokenExpiresAt().isAfter(now)) {
            return tokens.accessToken();
        }

        SpotifyAuthService.SpotifyTokenResponse tokenResponse =
                spotifyAuthService.refreshAccessToken(tokens.refreshToken());

        Instant expiresAt =
                spotifyTokenService.calculateAccessTokenExpiry(tokenResponse.expiresIn());

        spotifyTokenService.updateTokens(
                userId,
                tokenResponse.refreshToken(),
                tokenResponse.accessToken(),
                expiresAt
        );

        return tokenResponse.accessToken();
    }

    /* ======== RESPONSE RECORDS ======== */

    public record SpotifyTopArtistsResponse(List<SpotifyArtist> items) {}

    public record SpotifyArtist(
            String name,
            List<String> genres,
            List<SpotifyImage> images,
            SpotifyFollowers followers
    ) {}

    public record SpotifyFollowers(@JsonProperty("total") Integer total) {}

    public record SpotifyImage(String url, Integer height, Integer width) {}
}
