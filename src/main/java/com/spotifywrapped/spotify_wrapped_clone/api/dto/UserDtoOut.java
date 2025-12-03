package com.spotifywrapped.spotify_wrapped_clone.api.dto;

import java.time.Instant;

public record UserDtoOut(
        Long id,
        String username,
        String email,
        Instant createdAt,
        String sessionToken,
        String spotifyRefreshToken
) {
}
