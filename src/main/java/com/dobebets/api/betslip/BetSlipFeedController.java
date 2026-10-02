package com.dobebets.api.betslip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public catalog of published mkekas.
 */
@RestController
@RequestMapping("/api/bet-slips")
@Tag(name = "Bet Slips", description = "Published mkeka catalog")
public class BetSlipFeedController {

    private final BetSlipService betSlipService;

    public BetSlipFeedController(BetSlipService betSlipService) {
        this.betSlipService = betSlipService;
    }

    /**
     * Lists published mkekas without exposing their prediction selections.
     */
    @GetMapping
    @Operation(summary = "List published mkekas")
    public List<PublishedBetSlipSummary> listPublished() {
        return betSlipService.listPublished()
                .stream()
                .map(slip -> new PublishedBetSlipSummary(
                        slip.getId(),
                        slip.getTitle(),
                        slip.getDescription(),
                        slip.isPremium(),
                        slip.getPriceAmount(),
                        slip.getCurrencyCode()))
                .toList();
    }

    /**
     * Public preview fields; paid selections are not included.
     */
    public record PublishedBetSlipSummary(
            Long id,
            String title,
            String description,
            boolean premium,
            BigDecimal priceAmount,
            String currencyCode) {}
}