package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto.SpotifyStatusDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import com.spotifywrapped.spotify_wrapped_clone.service.SpotifyAuthService;
import com.spotifywrapped.spotify_wrapped_clone.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;
    private final UserService userService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SpotifyController(SpotifyAuthService spotifyAuthService, UserService userService) {
        this.spotifyAuthService = spotifyAuthService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> redirectToSpotify(
            @CookieValue(value = "sessionToken", required = false) String sessionToken) {

        User user = userService.findUserBySessionToken(sessionToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String state = newStateToken();
        String authorizationUrl = spotifyAuthService.buildAuthorizationUrl(state);

        ResponseCookie stateCookie = ResponseCookie.from("spotify_state", state)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(10))
                .path("/")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, authorizationUrl);
        headers.add(HttpHeaders.SET_COOKIE, stateCookie.toString());

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @CookieValue(value = "spotify_state", required = false) String stateCookie,
            @CookieValue(value = "sessionToken", required = false) String sessionToken) {

        if (stateCookie == null || !stateCookie.equals(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userService.findUserBySessionToken(sessionToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SpotifyAuthService.SpotifyTokenResponse tokenResponse = spotifyAuthService.exchangeCodeForToken(code);
        userService.updateSpotifyRefreshToken(user.getId(), tokenResponse.refreshToken());

        ResponseCookie deleteStateCookie = ResponseCookie.from("spotify_state", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(spotifyAuthService.getSuccessRedirect()));
        headers.add(HttpHeaders.SET_COOKIE, deleteStateCookie.toString());

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/status")
    public ResponseEntity<SpotifyStatusDto> spotifyStatus(
            @CookieValue(value = "sessionToken", required = false) String sessionToken) {

        User user = userService.findUserBySessionToken(sessionToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean connected = user.getSpotifyRefreshToken() != null && !user.getSpotifyRefreshToken().isBlank();
        return ResponseEntity.ok(new SpotifyStatusDto(connected));
    }

    private String newStateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
