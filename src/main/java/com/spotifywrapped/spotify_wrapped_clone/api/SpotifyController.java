package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyLoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyStatusDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyProfileDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.SpotifyAuthService;
import com.spotifywrapped.spotify_wrapped_clone.service.user_services.UserService;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.SpotifyTokenService;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.SpotifyProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import com.spotifywrapped.spotify_wrapped_clone.service.Security.JwtService;

import java.net.URI;
import java.time.Instant;


@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;
    private final UserService userService;
    private final SpotifyProfileService spotifyProfileService;
    private final SpotifyTokenService spotifyTokenService;
    private final JwtService jwtService;

    public SpotifyController(
            SpotifyAuthService spotifyAuthService,
            SpotifyProfileService spotifyProfileService,
            UserService userService,
            SpotifyTokenService spotifyTokenService,
            JwtService jwtService
    ) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyProfileService = spotifyProfileService;
        this.userService = userService;
        this.spotifyTokenService = spotifyTokenService;
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public ResponseEntity<SpotifyLoginDto> redirectToSpotify(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String state = jwtService.generateSpotifyStateToken(user.getId());
        String authorizationUrl = spotifyAuthService.buildAuthorizationUrl(state);

        return ResponseEntity.ok(new SpotifyLoginDto(authorizationUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        Long userId = jwtService.parseSpotifyStateUserId(state);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SpotifyAuthService.SpotifyTokenResponse tokenResponse = spotifyAuthService.exchangeCodeForToken(code);
        Instant expiresAt = spotifyTokenService.calculateAccessTokenExpiry(tokenResponse.expiresIn());
        System.out.println(expiresAt);
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
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean connected = spotifyTokenService.hasRefreshToken(user.getId());
        return ResponseEntity.ok(new SpotifyStatusDto(connected));
    }

    @GetMapping("/profile")
    public ResponseEntity<SpotifyProfileDto> spotifyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        User user = userService.findUserByAccessToken(extractBearerToken(authorization));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SpotifyProfileDto profile = spotifyProfileService.fetchProfile(user);
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            return ResponseEntity.ok(profile);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
