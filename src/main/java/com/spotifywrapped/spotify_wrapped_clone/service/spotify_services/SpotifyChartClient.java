package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpotifyChartClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyAuthService spotifyAuthService;

    // Playlist id
    @Value("${app.spotify.global-top-50-playlist-id:37i9dQZEVXbMDoHDwVN2tF}")
    private String globalTop50PlaylistId;

    public SpotifyChartClient(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

    public List<TrackItem> fetchGlobalTop50() {
        SpotifyAuthService.SpotifyTokenResponse tokenResponse = spotifyAuthService.requestClientCredentialsToken();
        String playlistUrl = "https://api.spotify.com/v1/playlists/" + globalTop50PlaylistId + "/tracks?limit=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenResponse.accessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PlaylistTracksResponse> response = restTemplate.exchange(
                playlistUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PlaylistTracksResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to load playlist tracks with status: " + response.getStatusCode());
        }

        List<PlaylistItem> items = response.getBody().items();
        if (items == null) {
            return List.of();
        }

        List<TrackItem> result = new ArrayList<>();
        for (PlaylistItem item : items) {
            if (item == null || item.track() == null || item.track().id() == null) {
                continue;
            }
            Track track = item.track();
            String artist = track.artists() == null
                    ? ""
                    : track.artists().stream()
                    .map(Artist::name)
                    .collect(Collectors.joining(", "));
            result.add(new TrackItem(track.id(), artist, track.name()));
        }
        return result;
    }

    public record TrackItem(String providerTrackId, String artist, String title) { }

    public record PlaylistTracksResponse(List<PlaylistItem> items) { }

    public record PlaylistItem(Track track) { }

    public record Track(String id,
                        String name,
                        @JsonProperty("artists") List<Artist> artists) { }

    public record Artist(String name) { }
}
