package com.spotifywrapped.spotify_wrapped_clone.api.dto.chart;

public record ChartEntryDto(
        Integer position,
        String title,
        String artist,
        String providerTrackId
) { }
