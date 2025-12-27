package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.chart.ChartEntryDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.chart.ChartSnapshotDto;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartEntry;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartSnapshot;
import com.spotifywrapped.spotify_wrapped_clone.service.ChartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/global-top-50")
    public ResponseEntity<ChartSnapshotDto> getGlobalTop50(
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        ChartSnapshot snapshot = chartService.findLatestGlobalTop50Snapshot();
        if (snapshot == null) {
            return ResponseEntity.noContent().build();
        }

        Integer boundedLimit = (limit == null || limit <= 0) ? null : Math.min(limit, 50);
        List<ChartEntry> entries = chartService.findEntriesForSnapshot(snapshot.getId(), boundedLimit);
        int totalEntries = chartService.countEntriesForSnapshot(snapshot.getId());

        List<ChartEntryDto> dtoEntries = entries.stream()
                .map(entry -> new ChartEntryDto(
                        entry.getPosition(),
                        entry.getSong().getTitle(),
                        entry.getSong().getArtist(),
                        entry.getSong().getProviderTrackId()
                ))
                .toList();

        ChartSnapshotDto dto = new ChartSnapshotDto(
                snapshot.getChartKey(),
                snapshot.getChartDate(),
                totalEntries,
                dtoEntries
        );

        return ResponseEntity.ok(dto);
    }
}
