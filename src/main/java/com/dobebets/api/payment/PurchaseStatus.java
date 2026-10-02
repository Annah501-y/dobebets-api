package com.dobebets.api.payment;

/**
 * Tracks a customer's manual payment and access review.
 */
public enum PurchaseStatus {
    AWAITING_PAYMENT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}