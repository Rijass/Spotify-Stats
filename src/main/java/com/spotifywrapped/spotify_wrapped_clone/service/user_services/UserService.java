package com.spotifywrapped.spotify_wrapped_clone.service.user_services;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.AuthResponseDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.LoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoIn;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoOut;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.UserDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import com.spotifywrapped.spotify_wrapped_clone.service.SensitiveDataService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class UserService {

    // ============================================================
    //                  Constants
    // ============================================================

    /** Dauer, wie lange ein Session-Token gültig bleibt */
    private static final Duration SESSION_DURATION = Duration.ofDays(30);


    // ============================================================
    //                  Dependencies
    // ============================================================

    private final UserDBaccess userDBaccess;
    private final SensitiveDataService sensitiveDataService;

    public UserService(UserDBaccess userDBaccess, SensitiveDataService sensitiveDataService) {
        this.userDBaccess = userDBaccess;
        this.sensitiveDataService = sensitiveDataService;
    }



    // ============================================================
    //                  Public API
    // ============================================================

    /**
     * Erstellt einen neuen User, erzeugt sichere Tokens und speichert ihn in der Datenbank.
     */
    public UserDtoOut createUser(UserDtoIn userDtoIn) {
        User user = mapToEntity(userDtoIn);

        // Session Token sicherstellen (falls der Client kein mitgegeben hat)
        String rawSessionToken = ensureSessionToken(user);

        // Passwort & Tokens verschlüsseln
        user.setPassword(sensitiveDataService.hashPassword(user.getPassword()));
        user.setSessionToken(sensitiveDataService.hashToken(user.getSessionToken()));
        user.setSessionExpiresAt(calculateSessionExpiry());
        user.setSpotifyRefreshToken(sensitiveDataService.encrypt(user.getSpotifyRefreshToken()));

        userDBaccess.createUser(user);

        return mapToDto(user, rawSessionToken);
    }

    /**
     * Aktualisiert die User-Daten. Nur Felder, die gesetzt sind, werden aktualisiert.
     */
    public UserDtoOut updateUser(Long id, UserDtoIn userDtoIn) {
        User userUpdates = mapToEntity(userDtoIn);

        if (userUpdates.getPassword() != null) {
            userUpdates.setPassword(sensitiveDataService.hashPassword(userUpdates.getPassword()));
        }
        if (userUpdates.getSessionToken() != null) {
            userUpdates.setSessionToken(sensitiveDataService.encrypt(userUpdates.getSessionToken()));
        }
        if (userUpdates.getSpotifyRefreshToken() != null) {
            userUpdates.setSpotifyRefreshToken(sensitiveDataService.encrypt(userUpdates.getSpotifyRefreshToken()));
        }
        if (userUpdates.getSpotifyAccessToken() != null) {
            userUpdates.setSpotifyAccessToken(sensitiveDataService.encrypt(userUpdates.getSpotifyAccessToken()));
        }

        // ExpiresAt muss nur gesetzt werden – kein Hashing nötig
        if (userUpdates.getSpotifyAccessTokenExpiresAt() != null) {
            userUpdates.setSpotifyAccessTokenExpiresAt(userUpdates.getSpotifyAccessTokenExpiresAt());
        }

        User updatedUser = userDBaccess.updateUser(id, userUpdates);
        if (updatedUser == null) {
            return null;
        }

        // Session Token entschlüsseln, um es zurückzugeben
        String sessionToken = sensitiveDataService.decrypt(updatedUser.getSessionToken());

        return mapToDto(updatedUser, sessionToken);
    }

    /**
     * Löscht einen User aus der Datenbank.
     */
    public boolean deleteUser(Long id) {
        return userDBaccess.deleteUser(id);
    }

    /**
     * Loggt einen User ein, prüft Passwort, generiert Token und gibt Auth-Daten zurück.
     */
    public AuthResponseDto login(LoginDto loginDto) {
        User user = userDBaccess.findByUsernameOrEmail(loginDto.identifier());

        // Nutzer existiert nicht oder Passwort falsch
        if (user == null || !sensitiveDataService.passwordMatches(loginDto.password(), user.getPassword())) {
            return null;
        }

        // Session Token erzeugen und speichern
        String rawSessionToken = sensitiveDataService.newSessionToken();
        Instant sessionExpiresAt = calculateSessionExpiry();
        String hashedToken = sensitiveDataService.hashToken(rawSessionToken);

        userDBaccess.updateSession(user.getId(), hashedToken, sessionExpiresAt);

        return new AuthResponseDto(user.getId(), user.getUsername(), user.getEmail(), rawSessionToken);
    }

    /**
     * Validiert ein Session Token (raw, also unverschlüsselt).
     */
    public boolean isSessionValid(String rawSessionToken) {
        if (rawSessionToken == null || rawSessionToken.isBlank()) {
            return false;
        }

        Instant now = Instant.now();

        return userDBaccess.findActiveSessions(now).stream()
                .anyMatch(u -> sensitiveDataService.tokenMatches(rawSessionToken, u.getSessionToken()));
    }

    /**
     * Findet den User anhand eines gültigen Session Tokens.
     */
    public User findUserBySessionToken(String rawSessionToken) {
        if (rawSessionToken == null || rawSessionToken.isBlank()) {
            return null;
        }

        Instant now = Instant.now();

        return userDBaccess.findActiveSessions(now).stream()
                .filter(u -> sensitiveDataService.tokenMatches(rawSessionToken, u.getSessionToken()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Aktualisiert nur das Spotify Refresh Token eines Users.
     */
    public void updateSpotifyRefreshToken(Long userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String encrypted = sensitiveDataService.encrypt(refreshToken);
        userDBaccess.updateSpotifyRefreshToken(userId, encrypted);
    }

    /**
     * Aktualisiert die Spotify OAuth Tokens + deren Ablaufzeit.
     */
    public void updateSpotifyTokens(Long userId, String refreshToken, String accessToken, Instant accessTokenExpiresAt) {
        if (accessToken == null || accessToken.isBlank() || accessTokenExpiresAt == null) {
            return;
        }

        String encryptedAccessToken = sensitiveDataService.encrypt(accessToken);
        String encryptedRefreshToken =
                (refreshToken == null || refreshToken.isBlank())
                        ? null
                        : sensitiveDataService.encrypt(refreshToken);

        userDBaccess.updateSpotifyTokens(
                userId,
                encryptedRefreshToken,
                encryptedAccessToken,
                accessTokenExpiresAt
        );
    }



    // ============================================================
    //                  Helper methods
    // ============================================================

    /**
     * Mappt DTO → Entity (reine Übertragung der Daten ohne Validierung).
     */
    private User mapToEntity(UserDtoIn dtoIn) {
        User user = new User();
        user.setUsername(dtoIn.username());
        user.setEmail(dtoIn.email());
        user.setPassword(dtoIn.password());
        user.setSessionToken(dtoIn.sessionToken());
        user.setSpotifyRefreshToken(dtoIn.spotifyRefreshToken());
        return user;
    }

    /**
     * Mappt Entity → DTO (mit entschlüsseltem Session Token und Spotify Token).
     */
    private UserDtoOut mapToDto(User user, String rawSessionToken) {
        return new UserDtoOut(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                rawSessionToken,
                sensitiveDataService.decrypt(user.getSpotifyRefreshToken())
        );
    }

    /**
     * Stellt sicher, dass der User ein Session Token hat.
     * Falls nicht → neues generieren.
     */
    private String ensureSessionToken(User user) {
        String sessionToken = user.getSessionToken();

        if (sessionToken == null || sessionToken.isBlank()) {
            sessionToken = sensitiveDataService.newSessionToken();
            user.setSessionToken(sessionToken);
        }

        return sessionToken;
    }

    /**
     * Berechnet Ablaufzeitpunkt einer Session.
     */
    private Instant calculateSessionExpiry() {
        return Instant.now().plus(SESSION_DURATION);
    }
}