package com.dobebets.api.match;

import java.time.OffsetDateTime;

public record MatchResponse(
        Long id,
        Long leagueId,
        String leagueName,
        String homeTeam,
        String awayTeam,
        OffsetDateTime kickoffAt,
        String status,
        Integer homeScore,
        Integer awayScore
) {
    static MatchResponse from(FootballMatch match) {
        return new MatchResponse(match.getId(), match.getLeague().getId(), match.getLeague().getName(),
                match.getHomeTeam(), match.getAwayTeam(), match.getKickoffAt(), match.getStatus(),
                match.getHomeScore(), match.getAwayScore());
    }
}
