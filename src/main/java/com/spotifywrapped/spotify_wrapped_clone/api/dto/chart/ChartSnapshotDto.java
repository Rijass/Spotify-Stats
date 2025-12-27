package com.spotifywrapped.spotify_wrapped_clone.api.dto.chart;

import java.time.LocalDate;
import java.util.List;

public record ChartSnapshotDto(
        String chartKey,
        LocalDate chartDate,
        int totalEntries,
        List<ChartEntryDto> entries
) { }
