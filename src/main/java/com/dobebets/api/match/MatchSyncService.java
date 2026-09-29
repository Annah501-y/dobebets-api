package com.dobebets.api.match;

import com.dobebets.api.football.ApiFootballClient;
import com.dobebets.api.league.League;
import com.dobebets.api.league.LeagueRepository;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Imports and updates fixtures from API-Football in the local database.
 */
@Service
public class MatchSyncService {

    private final ApiFootballClient apiFootballClient;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;

    public MatchSyncService(
            ApiFootballClient apiFootballClient,
            LeagueRepository leagueRepository,
            MatchRepository matchRepository) {
        this.apiFootballClient = apiFootballClient;
        this.leagueRepository = leagueRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Synchronizes fixtures for one UTC calendar date.
     *
     * @param date the fixture date requested from API-Football
     * @return number of fixtures inserted or updated
     */
    @Transactional
    public int syncFixtures(LocalDate date) {
        JsonNode apiResult = apiFootballClient.getFixturesByDate(date);
        JsonNode errors = apiResult.path("errors");

        // Stop the sync if the provider reports an API or parameter error.
        if ((errors.isObject() || errors.isArray()) && !errors.isEmpty()) {
            throw new IllegalStateException(
                    "API-Football reported errors: " + errors);
        }

        JsonNode fixtures = apiResult.path("response");
        if (!fixtures.isArray()) {
            throw new IllegalStateException(
                    "API-Football response did not contain a fixtures array.");
        }

        int processed = 0;

        for (JsonNode item : fixtures) {
            JsonNode fixtureData = item.path("fixture");
            JsonNode leagueData = item.path("league");
            JsonNode teamsData = item.path("teams");
            JsonNode goalsData = item.path("goals");

            long fixtureId = fixtureData.path("id").asLong(0);
            long apiFootballLeagueId = leagueData.path("id").asLong(0);

            String kickoffText = fixtureData.path("date").asString("");
            String status = fixtureData.path("status")
                    .path("short")
                    .asString("UNKNOWN");
            String homeTeam = teamsData.path("home").path("name").asString("");
            String awayTeam = teamsData.path("away").path("name").asString("");

            // Ignore incomplete provider records so they cannot violate database constraints.
            if (fixtureId <= 0
                    || apiFootballLeagueId <= 0
                    || kickoffText.isBlank()
                    || homeTeam.isBlank()
                    || awayTeam.isBlank()) {
                continue;
            }

            OffsetDateTime kickoffAt;
            try {
                kickoffAt = OffsetDateTime.parse(kickoffText);
            } catch (DateTimeParseException exception) {
                // Skip a fixture if its kickoff value is not a valid ISO date-time.
                continue;
            }

            // The fixture must reference a league already imported into our database.
            Optional<League> localLeague =
                    leagueRepository.findByApiFootballId(apiFootballLeagueId);
            if (localLeague.isEmpty()) {
                continue;
            }

            String externalId = Long.toString(fixtureId);
            Integer homeScore = readNullableScore(goalsData.path("home"));
            Integer awayScore = readNullableScore(goalsData.path("away"));

            Optional<FootballMatch> existingMatch =
                    matchRepository.findByExternalId(externalId);

            FootballMatch match;
            if (existingMatch.isPresent()) {
                match = existingMatch.get();
                match.updateFromApiFootball(
                        localLeague.get(),
                        homeTeam,
                        awayTeam,
                        kickoffAt,
                        status,
                        homeScore,
                        awayScore);
            } else {
                match = new FootballMatch(
                        localLeague.get(),
                        homeTeam,
                        awayTeam,
                        kickoffAt,
                        status,
                        homeScore,
                        awayScore,
                        externalId);
            }

            matchRepository.save(match);
            processed++;
        }

        return processed;
    }

    /**
     * Converts a provider score to an Integer, preserving null for unplayed matches.
     */
    private Integer readNullableScore(JsonNode scoreNode) {
        if (scoreNode.isMissingNode() || scoreNode.isNull()) {
            return null;
        }

        return scoreNode.asInt();
    }
}