package com.spotifywrapped.spotify_wrapped_clone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers chart ingestion once per day using UTC time to keep snapshots deterministic.
 */
@Component
public class ChartIngestionJob {

    private static final Logger log = LoggerFactory.getLogger(ChartIngestionJob.class);

    private final ChartService chartService;

    public ChartIngestionJob(ChartService chartService) {
        this.chartService = chartService;
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
}
