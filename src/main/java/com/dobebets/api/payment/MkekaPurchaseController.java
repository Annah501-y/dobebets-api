package com.dobebets.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer endpoints for beginning a manual mkeka purchase.
 */
@RestController
@RequestMapping("/api/bet-slips")
@Tag(name = "Mkeka Purchases", description = "Customer mkeka purchase requests")
public class MkekaPurchaseController {

    private final MkekaPurchaseService mkekaPurchaseService;

    public MkekaPurchaseController(MkekaPurchaseService mkekaPurchaseService) {
        this.mkekaPurchaseService = mkekaPurchaseService;
    }

    /**
     * Creates a pending purchase and returns the tipster's Lipa instructions.
     */
    @PostMapping("/{betSlipId}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a premium mkeka purchase")
    @SecurityRequirement(name = "basicAuth")
    public MkekaPurchaseService.PurchaseCheckoutResponse startPurchase(
            @PathVariable Long betSlipId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return mkekaPurchaseService.startPurchase(
                userDetails.getUsername(),
                betSlipId);
    }
}