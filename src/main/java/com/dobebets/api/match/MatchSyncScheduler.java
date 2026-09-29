package com.dobebets.api.match;

import com.dobebets.api.league.LeagueSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Refreshes recent and upcoming fixtures and the provider's current leagues.
 */
@Component
public class MatchSyncScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(MatchSyncScheduler.class);

    private final MatchSyncService matchSyncService;
    private final LeagueSyncService leagueSyncService;

    public MatchSyncScheduler(
            MatchSyncService matchSyncService,
            LeagueSyncService leagueSyncService) {
        this.matchSyncService = matchSyncService;
        this.leagueSyncService = leagueSyncService;
    }

    /**
     * Runs at 00:00, 06:00, 12:00, and 18:00 UTC while the app is running.
     */
    @Scheduled(cron = "0 0 0/6 * * *", zone = "UTC")
    public void syncRecentAndUpcomingFixtures() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // Refresh recent results, today's fixtures, and tomorrow's schedule.
        List<LocalDate> datesToSync = List.of(
                today.minusDays(1),
                today,
                today.plusDays(1));

        for (LocalDate date : datesToSync) {
            try {
                int processed = matchSyncService.syncFixtures(date);
                log.info("Fixture sync completed for {}: {} processed.", date, processed);
            } catch (RuntimeException exception) {
                // Continue with the other dates if one provider request fails.
                log.error("Fixture sync failed for {}.", date, exception);
            }
        }
    }

    /**
     * Refreshes provider league names and newly available competitions daily at 00:15 UTC.
     */
    @Scheduled(cron = "0 15 0 * * *", zone = "UTC")
    public void syncLeaguesDaily() {
        try {
            int processed = leagueSyncService.syncCurrentLeagues();
            log.info("Daily league sync completed: {} processed.", processed);
        } catch (RuntimeException exception) {
            // A league API failure is logged and does not stop fixture scheduling.
            log.error("Daily league sync failed.", exception);
        }
    }
}
