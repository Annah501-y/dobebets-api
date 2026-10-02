package com.dobebets.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tipster-only endpoints for reviewing customer payment submissions.
 */
@RestController
@RequestMapping("/api/admin/payments")
@Tag(name = "Admin - Payments", description = "Manual Lipa payment review")
public class PaymentReviewController {

    private final MkekaPurchaseService mkekaPurchaseService;

    public PaymentReviewController(MkekaPurchaseService mkekaPurchaseService) {
        this.mkekaPurchaseService = mkekaPurchaseService;
    }

    @GetMapping("/pending")
    @Operation(summary = "List payments awaiting review")
    @SecurityRequirement(name = "basicAuth")
    public List<MkekaPurchaseService.PaymentReviewResponse> listPending() {
        return mkekaPurchaseService.listPendingReviews();
    }

    @PatchMapping("/{purchaseId}/approve")
    @Operation(summary = "Approve a verified payment")
    @SecurityRequirement(name = "basicAuth")
    public MkekaPurchaseService.PaymentReviewResponse approve(
            @PathVariable Long purchaseId) {
        return mkekaPurchaseService.approvePayment(purchaseId);
    }

    @PatchMapping("/{purchaseId}/reject")
    @Operation(summary = "Reject an unverified payment")
    @SecurityRequirement(name = "basicAuth")
    public MkekaPurchaseService.PaymentReviewResponse reject(
            @PathVariable Long purchaseId) {
        return mkekaPurchaseService.rejectPayment(purchaseId);
    }
}