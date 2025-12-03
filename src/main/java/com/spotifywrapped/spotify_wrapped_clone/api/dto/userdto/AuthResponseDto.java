package com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto;

public record AuthResponseDto(
        Long id,
        String username,
        String email,
        String sessionToken
) {
}
