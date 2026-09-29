package com.dobebets.api.league;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Performs the initial league import when the application starts with an
 * empty leagues table.
 */
@Component
public class LeagueDataInitializer implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(LeagueDataInitializer.class);

    private final LeagueRepository leagueRepository;
    private final LeagueSyncService leagueSyncService;

    public LeagueDataInitializer(
            LeagueRepository leagueRepository,
            LeagueSyncService leagueSyncService) {
        this.leagueRepository = leagueRepository;
        this.leagueSyncService = leagueSyncService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (leagueRepository.count() > 0) {
            log.info("League records already exist; skipping initial import.");
            return;
        }

        int importedCount = leagueSyncService.syncCurrentLeagues();
        log.info("Initial API-Football import processed {} leagues.", importedCount);
    }
}