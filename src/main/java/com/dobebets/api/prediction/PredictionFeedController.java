package com.dobebets.api.prediction;

import com.dobebets.api.match.FootballMatch;
import com.dobebets.api.league.League;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Public read-only feed of predictions published by the platform's tipster.
 */
@RestController
@RequestMapping("/api/predictions")
@Tag(name = "Predictions", description = "Published tipster predictions")
public class PredictionFeedController {

    private final PredictionService predictionService;

    public PredictionFeedController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Lists published predictions with the match information users need.
     */
    @GetMapping
    @Operation(summary = "List published predictions")
    public List<PublishedPredictionResponse> listPublished() {
        return predictionService.listPublished()
                .stream()
                .map(PredictionFeedController::toResponse)
                .toList();
    }

    private static PublishedPredictionResponse toResponse(Prediction prediction) {
        FootballMatch match = prediction.getMatch();
        League league = match.getLeague();

        return new PublishedPredictionResponse(
                prediction.getId(),
                match.getId(),
                league.getName(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getKickoffAt(),
                prediction.getMarket(),
                prediction.getSelection(),
                prediction.getOdds(),
                prediction.getOutcome());
    }

    /**
     * Public response shape; it omits drafts and internal persistence details.
     */
    public record PublishedPredictionResponse(
            Long id,
            Long matchId,
            String leagueName,
            String homeTeam,
            String awayTeam,
            OffsetDateTime kickoffAt,
            PredictionMarket market,
            String selection,
            BigDecimal odds,
            PredictionOutcome outcome) {}
}