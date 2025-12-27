package com.spotifywrapped.spotify_wrapped_clone.service;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.ChartSnapshotDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.SongDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartEntry;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartSnapshot;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.Song;
import com.spotifywrapped.spotify_wrapped_clone.service.spotify_services.SpotifyChartClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ChartService {

    public static final String GLOBAL_TOP_50_KEY = "global-top-50";

    private final SpotifyChartClient spotifyChartClient;
    private final SongDBaccess songDBaccess;
    private final ChartSnapshotDBaccess chartSnapshotDBaccess;

    public ChartService(SpotifyChartClient spotifyChartClient,
                        SongDBaccess songDBaccess,
                        ChartSnapshotDBaccess chartSnapshotDBaccess) {
        this.spotifyChartClient = spotifyChartClient;
        this.songDBaccess = songDBaccess;
        this.chartSnapshotDBaccess = chartSnapshotDBaccess;
    }

    /**
     * Creates the daily snapshot for the Spotify Global Top 50 playlist.
     * Idempotent: if today's snapshot already exists with entries, it will not create duplicates.
     */
    public ChartSnapshot ingestDailyGlobalTop50() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        ChartSnapshot existingSnapshot = chartSnapshotDBaccess.findByChartKeyAndDate(GLOBAL_TOP_50_KEY, today);

        ChartSnapshot snapshot;
        if (existingSnapshot != null) {
            long existingEntries = chartSnapshotDBaccess.countEntriesForSnapshot(existingSnapshot.getId());
            if (existingEntries >= 50) {
                return existingSnapshot;
            }
            if (existingEntries > 0) {
                chartSnapshotDBaccess.deleteEntriesForSnapshot(existingSnapshot.getId());
            }
            snapshot = existingSnapshot;
        } else {
            snapshot = chartSnapshotDBaccess.createSnapshot(GLOBAL_TOP_50_KEY, today);
        }

        List<SpotifyChartClient.TrackItem> tracks = spotifyChartClient.fetchGlobalTop50();
        int position = 1;
        for (SpotifyChartClient.TrackItem track : tracks) {
            Song song = songDBaccess.saveOrUpdateSong(track.providerTrackId(), track.artist(), track.title());
            chartSnapshotDBaccess.addEntry(snapshot, song, position++);
        }

        return snapshot;
    }

    public ChartSnapshot findLatestGlobalTop50Snapshot() {
        return chartSnapshotDBaccess.findLatestSnapshot(GLOBAL_TOP_50_KEY);
    }

    public List<ChartEntry> findEntriesForSnapshot(Long snapshotId, Integer limit) {
        return chartSnapshotDBaccess.findEntriesForSnapshot(snapshotId, limit);
    }

    public int countEntriesForSnapshot(Long snapshotId) {
        return (int) chartSnapshotDBaccess.countEntriesForSnapshot(snapshotId);
    }
}
