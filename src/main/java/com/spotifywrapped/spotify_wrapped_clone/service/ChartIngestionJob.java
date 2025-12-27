package com.spotifywrapped.spotify_wrapped_clone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Holt sich Täglich die songs aus der Playlist
 */
@Component
public class ChartIngestionJob {

    private static final Logger log = LoggerFactory.getLogger(ChartIngestionJob.class);

    private final ChartService chartService;

    public ChartIngestionJob(ChartService chartService) {
        this.chartService = chartService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureInitialGlobalTop50Snapshot() {
        var latestSnapshot = chartService.findLatestGlobalTop50Snapshot();
        if (latestSnapshot == null) {
            ingestGlobalTop50("startup bootstrap (no snapshot)");
            return;
        }

        int existingEntries = chartService.countEntriesForSnapshot(latestSnapshot.getId());
        if (existingEntries == 0) {
            ingestGlobalTop50("startup bootstrap (empty snapshot)");
        }
    }

    @Scheduled(cron = "0 15 0 * * *", zone = "UTC")
    public void runDailyGlobalTop50Ingestion() {
        try {
            var snapshot = chartService.ingestDailyGlobalTop50();
            log.info("Global Top 50 snapshot ready for {} - {}", snapshot.getChartDate(), snapshot.getChartKey());
        } catch (Exception e) {
            log.error("Failed to ingest Global Top 50 snapshot", e);
        }
    }

    private void ingestGlobalTop50(String triggerContext) {
        try {
            var snapshot = chartService.ingestDailyGlobalTop50();
            log.info("Global Top 50 snapshot ready for {} - {} ({})", snapshot.getChartDate(), snapshot.getChartKey(), triggerContext);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn(
                    "Global Top 50 playlist (ID: {}) not found during {}. " +
                            "Please verify app.spotify.global-top-50-playlist-id.",
                    chartService.getGlobalTop50PlaylistId(),
                    triggerContext
            );
        } catch (Exception e) {
            log.error("Failed to ingest Global Top 50 snapshot during " + triggerContext, e);
        }
    }
}
