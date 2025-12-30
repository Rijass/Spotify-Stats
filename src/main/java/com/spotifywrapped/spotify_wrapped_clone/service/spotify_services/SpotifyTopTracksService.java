package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;


import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class SpotifyTopTracksService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyTokenService spotifyTokenService;

    @Value("https://api.spotify.com/v1/me/top/tracks?limit=10&time_range=medium_term")
    private String topTracksUri;

    public SpotifyTopTracksService(
            SpotifyAuthService spotifyAuthService,
            SpotifyTokenService spotifyTokenService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyTokenService = spotifyTokenService;
    }

    public List<SpotifyTopTrackDto> fetchTopTracks(User user) {
        if (user == null) return List.of();

        SpotifyTokenService.DecryptedSpotifyTokens tokens =
                spotifyTokenService.getDecryptedTokens(user.getId());

        if (tokens == null || tokens.refreshToken() == null || tokens.refreshToken().isBlank()) {
            return List.of();
        }

        String accessToken = getValidAccessToken(user.getId(), tokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<SpotifyTopTracksResponse> response =
                restTemplate.exchange(
                        topTracksUri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        SpotifyTopTracksResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to load top tracks: " + response.getStatusCode());
        }

        return response.getBody().items().stream()
                .map(track -> new SpotifyTopTrackDto(
                        track.name(),
                        track.artists().stream().map(SpotifyArtist::name).toList(),
                        track.album().images().getFirst().url()
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

    public record SpotifyTopTracksResponse(List<SpotifyTrack> items) {}

    public record SpotifyTrack(
            String name,
            List<SpotifyArtist> artists,
            SpotifyAlbum album
    ) {}

    public record SpotifyArtist(String name) {}

    public record SpotifyAlbum(List<SpotifyImage> images) {}

    public record SpotifyImage(String url, Integer height, Integer width) {}

    public record SpotifyTopTrackDto(
            String title,
            List<String> artists,
            String imageUrl
    ) {}
}


