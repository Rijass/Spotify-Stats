package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.*;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import com.spotifywrapped.spotify_wrapped_clone.service.JwtService;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.*;
import com.spotifywrapped.spotify_wrapped_clone.service.user_services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;
    private final UserService userService;
    private final SpotifyProfileService spotifyProfileService;
    private final SpotifyTokenService spotifyTokenService;
    private final JwtService jwtService;
    private final SpotifyTopTracksService spotifyTopTracksService;
    private final SpotifyTopArtistsService spotifyTopArtistsService;
    private final SpotifyFeaturedPlaylistsService spotifyFeaturedPlaylistsService;

    public SpotifyController(
            SpotifyAuthService spotifyAuthService,
            SpotifyProfileService spotifyProfileService,
            UserService userService,
            SpotifyTokenService spotifyTokenService,
            JwtService jwtService,
            SpotifyTopTracksService spotifyTopTracksService,
            SpotifyTopArtistsService spotifyTopArtistsService,
            SpotifyFeaturedPlaylistsService spotifyFeaturedPlaylistsService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyProfileService = spotifyProfileService;
        this.userService = userService;
        this.spotifyTokenService = spotifyTokenService;
        this.jwtService = jwtService;
        this.spotifyTopTracksService = spotifyTopTracksService;
        this.spotifyTopArtistsService = spotifyTopArtistsService;
        this.spotifyFeaturedPlaylistsService = spotifyFeaturedPlaylistsService;
    }

    /* ===== AUTH / STATUS ===== */

    @GetMapping("/login")
    public ResponseEntity<SpotifyLoginDto> redirectToSpotify(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String state = jwtService.generateSpotifyStateToken(user.getId());
        String authorizationUrl = spotifyAuthService.buildAuthorizationUrl(state);

        return ResponseEntity.ok(new SpotifyLoginDto(authorizationUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        Long userId = jwtService.parseSpotifyStateUserId(state);
        if (userId == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User user = userService.findUserById(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        SpotifyAuthService.SpotifyTokenResponse tokenResponse =
                spotifyAuthService.exchangeCodeForToken(code);

        Instant expiresAt =
                spotifyTokenService.calculateAccessTokenExpiry(tokenResponse.expiresIn());

        spotifyTokenService.updateTokens(
                user.getId(),
                tokenResponse.refreshToken(),
                tokenResponse.accessToken(),
                expiresAt
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(spotifyAuthService.getSuccessRedirect()))
                .build();
    }

    @GetMapping("/status")
    public ResponseEntity<SpotifyStatusDto> spotifyStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean connected = spotifyTokenService.hasRefreshToken(user.getId());
        return ResponseEntity.ok(new SpotifyStatusDto(connected));
    }

    /* ===== PROFILE ===== */

    @GetMapping("/profile")
    public ResponseEntity<SpotifyProfileDto> spotifyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            SpotifyProfileDto profile = spotifyProfileService.fetchProfile(user);
            if (profile == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.ok(profile);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /* ===== USER STATS ===== */

    @GetMapping("/top-tracks")
    public ResponseEntity<List<SpotifyTopTrackDto>> topTracks(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            return ResponseEntity.ok(spotifyTopTracksService.fetchTopTracks(user));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @GetMapping("/top-artists")
    public ResponseEntity<List<SpotifyTopArtistDto>> topArtists(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            return ResponseEntity.ok(spotifyTopArtistsService.fetchTopArtists(user));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /* ===== GLOBAL DISCOVERY ===== */

    @GetMapping("/featured-playlists")
    public ResponseEntity<List<SpotifyFeaturedPlaylistDto>> featuredPlaylists(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            return ResponseEntity.ok(
                    spotifyFeaturedPlaylistsService.fetchFeaturedPlaylists(user)
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /* ===== HELPER ===== */

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return null;
        return authorization.substring("Bearer ".length()).trim();
    }
}
