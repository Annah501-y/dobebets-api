package com.dobebets.api.prediction;

import com.dobebets.api.match.FootballMatch;
import com.dobebets.api.match.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Applies business rules when the tipster creates a prediction.
 */
@Service
public class PredictionService {

    private static final Pattern GOALS_LINE =
            Pattern.compile("(OVER|UNDER)\\s+\\d+(\\.\\d+)?");

    private static final Pattern CORRECT_SCORE =
            Pattern.compile("\\d{1,2}-\\d{1,2}");

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public PredictionService(
            MatchRepository matchRepository,
            PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    /**
     * Validates the submitted prediction and saves it as a draft.
     */
    @Transactional
    public Prediction createDraft(
            Long matchId,
            PredictionMarket market,
            String selection,
            BigDecimal odds) {

        if (matchId == null) {
            throw new IllegalArgumentException("A match ID is required.");
        }
        if (market == null) {
            throw new IllegalArgumentException("A prediction market is required.");
        }
        if (odds == null || odds.compareTo(BigDecimal.ONE) <= 0) {
            throw new IllegalArgumentException(
                    "Decimal odds must be greater than 1.0.");
        }

        FootballMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No match exists with ID " + matchId + "."));

        String normalizedSelection = validateAndNormalizeSelection(market, selection);

        Prediction prediction = new Prediction(
                match,
                market,
                normalizedSelection,
                odds);

        return predictionRepository.save(prediction);
    }

    /**
     * Publishes an existing draft prediction.
     */
    @Transactional
    public Prediction publishDraft(Long predictionId) {
        if (predictionId == null) {
            throw new IllegalArgumentException("A prediction ID is required.");
        }

        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No prediction exists with ID " + predictionId + "."));

        prediction.publish();
        return prediction;
    }

        /**
     * Returns published predictions for the public feed.
     */
        @Transactional(readOnly = true)
        public List<Prediction> listPublished() {
            return predictionRepository.findWithMatchByStatus(
                    PredictionStatus.PUBLISHED);
        }

    /**
     * Validates selection formats appropriate to each supported market.
     */
    private String validateAndNormalizeSelection(
            PredictionMarket market,
            String selection) {

        if (selection == null || selection.isBlank()) {
            throw new IllegalArgumentException("A prediction selection is required.");
        }

        String normalized = selection.trim().toUpperCase(Locale.ROOT);

        boolean valid = switch (market) {
            case ONE_X_TWO ->
                    normalized.equals("HOME")
                            || normalized.equals("DRAW")
                            || normalized.equals("AWAY");
            case DOUBLE_CHANCE ->
                    normalized.equals("1X")
                            || normalized.equals("X2")
                            || normalized.equals("12");
            case OVER_UNDER -> GOALS_LINE.matcher(normalized).matches();
            case BOTH_TEAMS_TO_SCORE ->
                    normalized.equals("YES") || normalized.equals("NO");
            case CORRECT_SCORE -> CORRECT_SCORE.matcher(normalized).matches();
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Selection format is invalid for market " + market + ".");
        }

        return normalized;
    }
}
