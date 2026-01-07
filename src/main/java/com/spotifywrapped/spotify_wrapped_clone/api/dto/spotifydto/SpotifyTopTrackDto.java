package com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto;

import java.util.List;

public record SpotifyTopTrackDto(
        String title,
        List<String> artists,
        String imageUrl
) {}