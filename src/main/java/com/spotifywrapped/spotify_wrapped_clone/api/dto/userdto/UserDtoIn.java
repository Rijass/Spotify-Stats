package com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto;

public record UserDtoIn(
        String username,
        String email,
        String password,
        String sessionToken
) {
}
