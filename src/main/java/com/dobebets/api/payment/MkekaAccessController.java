package com.dobebets.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer endpoint for reading selections after access is authorized.
 */
@RestController
@RequestMapping("/api/bet-slips")
@Tag(name = "Mkeka Access", description = "Access to mkeka selections")
public class MkekaAccessController {

    private final MkekaAccessService mkekaAccessService;

    public MkekaAccessController(MkekaAccessService mkekaAccessService) {
        this.mkekaAccessService = mkekaAccessService;
    }

    /**
     * Returns a free mkeka or a premium mkeka with approved customer access.
     */
    @GetMapping("/{betSlipId}/selections")
    @Operation(summary = "View accessible mkeka selections")
    @SecurityRequirement(name = "basicAuth")
    public MkekaAccessService.PurchasedMkekaResponse getSelections(
            @PathVariable Long betSlipId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return mkekaAccessService.getSelections(
                userDetails.getUsername(),
                betSlipId);
    }
}