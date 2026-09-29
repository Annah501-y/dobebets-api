package com.dobebets.api.league;

import com.dobebets.api.football.ApiFootballClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Imports current leagues from API-Football into the local database.
 */
@Service
public class LeagueSyncService {

    private final ApiFootballClient apiFootballClient;
    private final LeagueRepository leagueRepository;

    public LeagueSyncService(
            ApiFootballClient apiFootballClient,
            LeagueRepository leagueRepository) {
        this.apiFootballClient = apiFootballClient;
        this.leagueRepository = leagueRepository;
    }

    /**
     * Inserts new leagues and updates leagues already known by provider ID.
     *
     * @return number of valid league records processed
     */
    @Transactional
    public int syncCurrentLeagues() {
        JsonNode apiResult = apiFootballClient.getCurrentLeagues();
        JsonNode errors = apiResult.path("errors");

        if ((errors.isObject() || errors.isArray()) && !errors.isEmpty()) {
            throw new IllegalStateException(
                    "API-Football reported errors: " + errors);
        }

        JsonNode response = apiResult.path("response");
        if (!response.isArray()) {
            throw new IllegalStateException(
                    "API-Football response did not contain a leagues array.");
        }

        int processed = 0;

        for (JsonNode item : response) {
            JsonNode providerLeague = item.path("league");

            long providerId = providerLeague.path("id").asLong(0);
            String name = providerLeague.path("name").asString("");
            String country = item.path("country").path("name").asString("");

            // Ignore incomplete provider records because the database requires these fields.
            if (providerId <= 0 || name.isBlank() || country.isBlank()) {
                continue;
            }

            League localLeague = leagueRepository
                    .findByApiFootballId(providerId)
                    .orElseGet(() -> new League(name, country, null, true));

            // Preserve the existing optional code when the provider omits it.
            String providerCode = providerLeague.path("code").hasNonNull("code")
                    ? providerLeague.path("code").asString()
                    : null;
            String code = providerCode == null
                    ? localLeague.getCode()
                    : providerCode;

            localLeague.updateFromApiFootball(
                    providerId,
                    name,
                    country,
                    code);

            leagueRepository.save(localLeague);
            processed++;
        }

        return processed;
    }
}
