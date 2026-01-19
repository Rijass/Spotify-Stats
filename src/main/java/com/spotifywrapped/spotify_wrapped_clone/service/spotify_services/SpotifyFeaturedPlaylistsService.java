package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyFeaturedPlaylistDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class SpotifyFeaturedPlaylistsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyTokenService spotifyTokenService;

    @Value("https://api.spotify.com/v1/search?q=*&type=playlist&limit=10&market=DE")
    private String featuredPlaylistsUri;

    public SpotifyFeaturedPlaylistsService(
            SpotifyAuthService spotifyAuthService,
            SpotifyTokenService spotifyTokenService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyTokenService = spotifyTokenService;
    }

    public List<SpotifyFeaturedPlaylistDto> fetchFeaturedPlaylists(User user) {
        if (user == null) return List.of();

        SpotifyTokenService.DecryptedSpotifyTokens tokens =
                spotifyTokenService.getDecryptedTokens(user.getId());

        if (tokens == null || tokens.refreshToken() == null || tokens.refreshToken().isBlank()) {
            return List.of();
        }

        String accessToken = getValidAccessToken(user.getId(), tokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<SpotifyFeaturedPlaylistsResponse> response =
                restTemplate.exchange(
                        featuredPlaylistsUri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        SpotifyFeaturedPlaylistsResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException(
                    "Failed to load featured playlists: " + response.getStatusCode()
            );
        }

        return response.getBody().playlists().items().stream()
                .map(playlist -> new SpotifyFeaturedPlaylistDto(
                        playlist.name(),
                        playlist.description(),
                        playlist.owner().displayName(),
                        playlist.tracks().total(),
                        playlist.images() != null && !playlist.images().isEmpty()
                                ? playlist.images().getFirst().url()
                                : null,
                        playlist.externalUrls().spotify()
                ))
                .toList();
    }

    private String getValidAccessToken(
            Long userId,
            SpotifyTokenService.DecryptedSpotifyTokens tokens
    ) {
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

    public record SpotifyFeaturedPlaylistsResponse(SpotifyPlaylists playlists) {}

    public record SpotifyPlaylists(List<SpotifyPlaylist> items) {}

    public record SpotifyPlaylist(
            String name,
            String description,
            SpotifyOwner owner,
            SpotifyTracks tracks,
            List<SpotifyImage> images,
            @JsonProperty("external_urls") SpotifyExternalUrls externalUrls
    ) {}

    public record SpotifyOwner(
            @JsonProperty("display_name") String displayName
    ) {}

    public record SpotifyTracks(@JsonProperty("total") Integer total) {}

    public record SpotifyExternalUrls(String spotify) {}

    public record SpotifyImage(String url, Integer height, Integer width) {}
}
