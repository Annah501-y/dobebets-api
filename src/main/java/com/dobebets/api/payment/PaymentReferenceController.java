package com.dobebets.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer endpoint for submitting a Lipa transaction reference.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Manual Lipa payment submissions")
public class PaymentReferenceController {

    private final MkekaPurchaseService mkekaPurchaseService;

    public PaymentReferenceController(MkekaPurchaseService mkekaPurchaseService) {
        this.mkekaPurchaseService = mkekaPurchaseService;
    }

    /**
     * Submits the reference for a purchase owned by the signed-in customer.
     */
    @PostMapping("/{purchaseId}/reference")
    @Operation(summary = "Submit a Lipa payment reference")
    @SecurityRequirement(name = "basicAuth")
    public MkekaPurchaseService.PaymentReferenceResponse submitReference(
            @PathVariable Long purchaseId,
            @Valid @RequestBody SubmitReferenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return mkekaPurchaseService.submitPaymentReference(
                userDetails.getUsername(),
                purchaseId,
                request.transactionReference());
    }

    /**
     * Validates the reference text before service-level ownership checks.
     */
    public record SubmitReferenceRequest(
            @NotBlank @Size(max = 80) String transactionReference) {}

            /**
     * Lets the signed-in customer check their purchase progress.
     */
    @GetMapping("/{purchaseId}")
    @Operation(summary = "Check a purchase payment status")
    @SecurityRequirement(name = "basicAuth")
    public MkekaPurchaseService.CustomerPurchaseStatusResponse getPurchaseStatus(
            @PathVariable Long purchaseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return mkekaPurchaseService.getCustomerPurchaseStatus(
                userDetails.getUsername(),
                purchaseId);
    }    
}