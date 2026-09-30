package com.dobebets.api.betslip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
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
 * Administrative endpoints for the platform's single tipster.
 */
@RestController
@RequestMapping("/api/admin/bet-slips")
@Tag(name = "Admin - Bet Slips", description = "Tipster mkeka management")
public class BetSlipAdminController {

    private final BetSlipService betSlipService;

    public BetSlipAdminController(BetSlipService betSlipService) {
        this.betSlipService = betSlipService;
    }

    /**
     * Creates an unpublished mkeka; its currency is always Tanzanian shillings.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft mkeka")
    @SecurityRequirement(name = "basicAuth")
    public BetSlipResponse createDraft(
            @Valid @RequestBody CreateBetSlipRequest request) {

        BetSlip slip = betSlipService.createDraft(
                request.title(),
                request.description(),
                request.premium(),
                request.priceAmount(),
                "TZS");

        return toResponse(slip);
    }

    /**
     * Adds a draft prediction to a draft mkeka.
     */
    @PostMapping("/{betSlipId}/predictions/{predictionId}")
    @Operation(summary = "Add a draft prediction to a draft mkeka")
    @SecurityRequirement(name = "basicAuth")
    public BetSlipResponse addPrediction(
            @PathVariable Long betSlipId,
            @PathVariable Long predictionId) {

        BetSlip slip = betSlipService.addPrediction(betSlipId, predictionId);
        return toResponse(slip);
    }

    /**
     * Returns the mkeka summary without exposing JPA relationship objects.
     */
    private BetSlipResponse toResponse(BetSlip slip) {
        return new BetSlipResponse(
                slip.getId(),
                slip.getTitle(),
                slip.getDescription(),
                slip.isPremium(),
                slip.getPriceAmount(),
                slip.getCurrencyCode(),
                slip.getStatus());
    }

    /**
     * Validates the incoming mkeka details before the service applies business
     * rules.
     */
    public record CreateBetSlipRequest(
            @NotBlank @Size(max = 120) String title,
            @Size(max = 500) String description,
            boolean premium,
            @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal priceAmount) {
    }

    /**
     * Small response shape for confirming admin operations.
     */
    public record BetSlipResponse(
            Long id,
            String title,
            String description,
            boolean premium,
            BigDecimal priceAmount,
            String currencyCode,
            BetSlipStatus status) {
    }

    /**
     * Publishes a completed mkeka for the next publication workflow.
     */
    @PatchMapping("/{betSlipId}/publish")
    @Operation(summary = "Publish a draft mkeka")
    @SecurityRequirement(name = "basicAuth")
    public BetSlipResponse publishDraft(
            @PathVariable Long betSlipId) {

        BetSlip slip = betSlipService.publishDraft(betSlipId);
        return toResponse(slip);
    }
}