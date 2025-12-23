package com.spotifywrapped.spotify_wrapped_clone.service.user_services;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.AuthResponseDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.LoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoIn;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoOut;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.UserDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import com.spotifywrapped.spotify_wrapped_clone.service.JwtService;
import com.spotifywrapped.spotify_wrapped_clone.service.SensitiveDataService;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // ============================================================
    //                  Dependencies
    // ============================================================

    private final UserDBaccess userDBaccess;
    private final SensitiveDataService sensitiveDataService;
    private final JwtService jwtService;

    public UserService(UserDBaccess userDBaccess, SensitiveDataService sensitiveDataService, JwtService jwtService) {
        this.userDBaccess = userDBaccess;
        this.sensitiveDataService = sensitiveDataService;
        this.jwtService = jwtService;
    }


    // ============================================================
    //                  Public API
    // ============================================================

    /**
     * Erstellt einen neuen User, erzeugt sichere Tokens und speichert ihn in der Datenbank.
     */
    public UserDtoOut createUser(UserDtoIn userDtoIn) {
        User user = mapToEntity(userDtoIn);

        // Passwort verschlüsseln
        user.setPassword(sensitiveDataService.hashPassword(user.getPassword()));

        userDBaccess.createUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        return mapToDto(user, accessToken);
    }

    /**
     * Aktualisiert die User-Daten. Nur Felder, die gesetzt sind, werden aktualisiert.
     */
    public UserDtoOut updateUser(Long id, UserDtoIn userDtoIn) {
        User userUpdates = mapToEntity(userDtoIn);

        if (userUpdates.getPassword() != null) {
            userUpdates.setPassword(sensitiveDataService.hashPassword(userUpdates.getPassword()));
        }

        User updatedUser = userDBaccess.updateUser(id, userUpdates);
        if (updatedUser == null) {
            return null;
        }

        return mapToDto(updatedUser, null);
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

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponseDto(user.getId(), user.getUsername(), user.getEmail(), accessToken);
    }

    public boolean isAccessTokenValid(String accessToken) {
        return jwtService.parseUserId(accessToken) != null;
    }

    public User findUserByAccessToken(String accessToken) {
        Long userId = jwtService.parseUserId(accessToken);
        if (userId == null) {
            return null;
        }
        return userDBaccess.findUserById(userId);
    }

    public User findUserById(Long id) {
        return userDBaccess.findUserById(id);
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
        return user;
    }

    /**
     * Mappt Entity → DTO (inkl. Access Token).
     */
    private UserDtoOut mapToDto(User user, String accessToken) {
        return new UserDtoOut(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                accessToken
        );
    }
}