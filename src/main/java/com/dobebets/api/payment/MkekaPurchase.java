package com.dobebets.api.payment;

import com.dobebets.api.account.CustomerAccount;
import com.dobebets.api.betslip.BetSlip;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Records one customer's purchase attempt for a mkeka.
 */
@Entity
@Table(name = "mkeka_purchases")
public class MkekaPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount customerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_slip_id", nullable = false)
    private BetSlip betSlip;

    // Snapshot the price so later price changes do not alter this purchase attempt.
    @Column(name = "amount_tzs", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountTzs;

    @Column(name = "transaction_reference", unique = true, length = 80)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseStatus status = PurchaseStatus.AWAITING_PAYMENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    protected MkekaPurchase() {
        // JPA requires a no-argument constructor.
    }

    public MkekaPurchase(
            CustomerAccount customerAccount,
            BetSlip betSlip,
            BigDecimal amountTzs) {
        this.customerAccount = customerAccount;
        this.betSlip = betSlip;
        this.amountTzs = amountTzs;
    }

    @PrePersist
    void setCreatedAt() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Submits the mobile-money reference for tipster review.
     */
    public void submitPaymentReference(String reference) {
        if (status != PurchaseStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException(
                    "This purchase is not awaiting a payment reference.");
        }

        if (reference == null || reference.isBlank() || reference.trim().length() > 80) {
            throw new IllegalArgumentException(
                    "Enter a payment reference of 1 to 80 characters.");
        }

        this.transactionReference = reference.trim();
        this.status = PurchaseStatus.PENDING_REVIEW;
        this.submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Grants access after the tipster confirms the payment.
     */
    public void approve() {
        if (status != PurchaseStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Only a payment awaiting review can be approved.");
        }

        this.status = PurchaseStatus.APPROVED;
        this.reviewedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Rejects a submitted payment that the tipster cannot verify.
     */
    public void reject() {
        if (status != PurchaseStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Only a payment awaiting review can be rejected.");
        }

        this.status = PurchaseStatus.REJECTED;
        this.reviewedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public CustomerAccount getCustomerAccount() {
        return customerAccount;
    }

    public BetSlip getBetSlip() {
        return betSlip;
    }

    public BigDecimal getAmountTzs() {
        return amountTzs;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }
}