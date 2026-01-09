package com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto;

import java.util.List;

public record SpotifyTopArtistDto(
        String name,
        List<String> genres,
        String imageUrl,
        Integer followers
) {}
