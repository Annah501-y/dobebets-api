package com.dobebets.api.match;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Provides read-only endpoints for searching football fixtures.
 */
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Football fixtures and results")
public class MatchController {

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Returns matches for a UTC calendar date, with optional league and team filters.
     */
    @GetMapping
    @Operation(summary = "Search matches by date, league, and team")
    public List<MatchResponse> listMatches(
            @Parameter(description = "Date to search in UTC. Defaults to today.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @Parameter(description = "ID of the league to filter by")
            @RequestParam(required = false)
            Long leagueId,

            @Parameter(description = "Case-insensitive part of either team's name")
            @RequestParam(required = false)
            String search) {

        // Use UTC consistently so the same date range is applied regardless of server location.
        LocalDate selectedDate = date == null
                ? LocalDate.now(ZoneOffset.UTC)
                : date;

        OffsetDateTime startOfDay = selectedDate
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime startOfNextDay = selectedDate
                .plusDays(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        // Treat missing or blank search text as no team-name filter.
        String normalizedSearch = search == null || search.isBlank()
                ? ""
                : search.trim();

        return matchRepository
                .search(leagueId, startOfDay, startOfNextDay, normalizedSearch)
                .stream()
                .map(MatchResponse::from)
                .toList();
    }
}
