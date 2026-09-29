package com.dobebets.api.prediction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Administrative endpoints used by the platform's single tipster.
 */
@RestController
@RequestMapping("/api/admin/predictions")
@Tag(name = "Admin - Predictions", description = "Tipster prediction management")
public class PredictionAdminController {

    private final PredictionService predictionService;

    public PredictionAdminController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Creates a prediction as a draft; drafts are not part of the public feed.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft prediction")
    @SecurityRequirement(name = "basicAuth")
    public PredictionResponse createDraft(
            @Valid @RequestBody CreatePredictionRequest request) {

        Prediction prediction = predictionService.createDraft(
                request.matchId(),
                request.market(),
                request.selection(),
                request.odds());

        return toResponse(prediction);
    }

    /**
     * Publishes one saved draft prediction for the future public feed.
     */
    @PatchMapping("/{predictionId}/publish")
    @Operation(summary = "Publish a draft prediction")
    @SecurityRequirement(name = "basicAuth")
    public PredictionResponse publishDraft(
            @PathVariable Long predictionId) {

        Prediction prediction = predictionService.publishDraft(predictionId);
        return toResponse(prediction);
    }

    /**
     * Converts the entity to a response DTO and avoids serializing JPA relationships.
     */
    private PredictionResponse toResponse(Prediction prediction) {
        return new PredictionResponse(
                prediction.getId(),
                prediction.getMatch().getId(),
                prediction.getMarket(),
                prediction.getSelection(),
                prediction.getOdds(),
                prediction.getStatus(),
                prediction.getOutcome());
    }

    /**
     * Validated request fields protect the service from malformed JSON input.
     */
    public record CreatePredictionRequest(
            @NotNull Long matchId,
            @NotNull PredictionMarket market,
            @NotBlank @Size(max = 60) String selection,
            @NotNull
            @DecimalMin(value = "1.0", inclusive = false)
            @Digits(integer = 5, fraction = 3)
            BigDecimal odds) {}

    /**
     * Response fields needed to confirm a prediction operation.
     */
    public record PredictionResponse(
            Long id,
            Long matchId,
            PredictionMarket market,
            String selection,
            BigDecimal odds,
            PredictionStatus status,
            PredictionOutcome outcome) {}
}
