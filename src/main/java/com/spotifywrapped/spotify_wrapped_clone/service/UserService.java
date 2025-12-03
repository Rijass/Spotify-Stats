package com.spotifywrapped.spotify_wrapped_clone.service;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.AuthResponseDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.LoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.UserDtoIn;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.UserDtoOut;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.UserDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDBaccess userDBaccess;
    private final SensitiveDataService sensitiveDataService;

    public UserService(UserDBaccess userDBaccess, SensitiveDataService sensitiveDataService) {
        this.userDBaccess = userDBaccess;
        this.sensitiveDataService = sensitiveDataService;
    }

    public UserDtoOut createUser(UserDtoIn userDtoIn) {
        User user = mapToEntity(userDtoIn);
        String rawSessionToken = ensureSessionToken(user);
        user.setPassword(sensitiveDataService.hashPassword(user.getPassword()));
        user.setSessionToken(sensitiveDataService.encrypt(user.getSessionToken()));
        user.setSpotifyRefreshToken(sensitiveDataService.encrypt(user.getSpotifyRefreshToken()));

        userDBaccess.createUser(user);

        return mapToDto(user, rawSessionToken);
    }

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

        User updatedUser = userDBaccess.updateUser(id, userUpdates);
        if (updatedUser == null) {
            return null;
        }

        String sessionToken = sensitiveDataService.decrypt(updatedUser.getSessionToken());
        return mapToDto(updatedUser, sessionToken);
    }

    public boolean deleteUser(Long id) {
        return userDBaccess.deleteUser(id);
    }

    public AuthResponseDto login(LoginDto loginDto) {
        User user = userDBaccess.findByUsernameOrEmail(loginDto.identifier());
        if (user == null || !sensitiveDataService.passwordMatches(loginDto.password(), user.getPassword())) {
            return null;
        }

        String rawSessionToken = sensitiveDataService.newSessionToken();
        user.setSessionToken(sensitiveDataService.encrypt(rawSessionToken));
        return new AuthResponseDto(user.getId(), user.getUsername(), user.getEmail(), rawSessionToken);
    }

    private User mapToEntity(UserDtoIn dtoIn) {
        User user = new User();
        user.setUsername(dtoIn.username());
        user.setEmail(dtoIn.email());
        user.setPassword(dtoIn.password());
        user.setSessionToken(dtoIn.sessionToken());
        user.setSpotifyRefreshToken(dtoIn.spotifyRefreshToken());
        return user;
    }

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

    private String ensureSessionToken(User user) {
        String sessionToken = user.getSessionToken();
        if (sessionToken == null || sessionToken.isBlank()) {
            sessionToken = sensitiveDataService.newSessionToken();
            user.setSessionToken(sessionToken);
        }
        return sessionToken;
    }
}
