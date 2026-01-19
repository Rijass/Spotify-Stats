package com.spotifywrapped.spotify_wrapped_clone.api.dto.spotifydto;

public record SpotifyFeaturedPlaylistDto(
        String name,
        String description,
        String ownerName,
        Integer trackCount,
        String imageUrl,
        String spotifyUrl
) {}
