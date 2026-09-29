package com.dobebets.api.match;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Provides protected administrative operations for match data.
 */
@RestController
@RequestMapping("/api/admin/matches")
@Tag(name = "Admin - Matches", description = "Administrative fixture operations")
public class MatchSyncController {

    private final MatchSyncService matchSyncService;

    public MatchSyncController(MatchSyncService matchSyncService) {
        this.matchSyncService = matchSyncService;
    }

    /**
     * Fetches fixtures for a date and saves or updates them in PostgreSQL.
     */
    @PostMapping("/sync")
    @Operation(
            summary = "Synchronize fixtures for a date",
            description = "Fetches fixtures from API-Football and saves them locally.")
    @SecurityRequirement(name = "basicAuth")
    public FixtureSyncResponse syncFixtures(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        // Default to today's UTC date when no date is supplied.
        LocalDate selectedDate = date == null
                ? LocalDate.now(ZoneOffset.UTC)
                : date;

        int processed = matchSyncService.syncFixtures(selectedDate);
        return new FixtureSyncResponse(selectedDate, processed);
    }

    /**
     * Reports which date was synchronized and how many fixtures were saved or updated.
     */
    public record FixtureSyncResponse(LocalDate date, int processed) {}
}