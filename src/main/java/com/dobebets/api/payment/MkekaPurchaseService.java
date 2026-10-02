package com.dobebets.api.payment;

import com.dobebets.api.account.CustomerAccount;
import com.dobebets.api.account.CustomerAccountRepository;
import com.dobebets.api.betslip.BetSlip;
import com.dobebets.api.betslip.BetSlipRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Starts a manual Lipa-number purchase for a published premium mkeka.
 */
@Service
public class MkekaPurchaseService {

    private final MkekaPurchaseRepository purchaseRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final BetSlipRepository betSlipRepository;
    private final String tipsterLipaNumber;

    public MkekaPurchaseService(
            MkekaPurchaseRepository purchaseRepository,
            CustomerAccountRepository customerAccountRepository,
            BetSlipRepository betSlipRepository,
            @Value("${tipster.lipa-number}") String tipsterLipaNumber) {
        this.purchaseRepository = purchaseRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.betSlipRepository = betSlipRepository;
        this.tipsterLipaNumber = tipsterLipaNumber;
    }

    /**
     * Creates a purchase record and returns payment instructions to the customer.
     */
    @Transactional
    public PurchaseCheckoutResponse startPurchase(
            String customerPhoneNumber,
            Long betSlipId) {

        CustomerAccount customer = customerAccountRepository
                .findByPhoneNumber(customerPhoneNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer account was not found."));

        BetSlip betSlip = betSlipRepository.findById(betSlipId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mkeka was not found."));

        if (betSlip.getStatus() != com.dobebets.api.betslip.BetSlipStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Only published mkekas can be purchased.");
        }

        if (!betSlip.isPremium()) {
            throw new IllegalStateException(
                    "This mkeka is free and does not need a purchase.");
        }

        List<PurchaseStatus> existingPurchaseStatuses = List.of(
                PurchaseStatus.AWAITING_PAYMENT,
                PurchaseStatus.PENDING_REVIEW,
                PurchaseStatus.APPROVED);

        boolean purchaseAlreadyExists =
                purchaseRepository.existsByCustomerAccount_IdAndBetSlip_IdAndStatusIn(
                        customer.getId(),
                        betSlip.getId(),
                        existingPurchaseStatuses);

        if (purchaseAlreadyExists) {
            throw new IllegalStateException(
                    "You already have a purchase request or approved access for this mkeka.");
        }

        BigDecimal amountTzs = betSlip.getPriceAmount();

        MkekaPurchase purchase = purchaseRepository.save(
                new MkekaPurchase(customer, betSlip, amountTzs));

        return new PurchaseCheckoutResponse(
                purchase.getId(),
                betSlip.getTitle(),
                amountTzs,
                "TZS",
                tipsterLipaNumber,
                purchase.getStatus());
    }

    /**
     * Payment instructions shown before the customer sends money.
     */
    public record PurchaseCheckoutResponse(
            Long purchaseId,
            String mkekaTitle,
            BigDecimal amountTzs,
            String currencyCode,
            String tipsterLipaNumber,
            PurchaseStatus status) {}

             /**
     * Submits a payment reference for the authenticated customer’s purchase.
     */
    @Transactional
    public PaymentReferenceResponse submitPaymentReference(
            String customerPhoneNumber,
            Long purchaseId,
            String reference) {

        CustomerAccount customer = customerAccountRepository
                .findByPhoneNumber(customerPhoneNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer account was not found."));

        MkekaPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase was not found for this customer."));

        // Customers may submit references only for their own purchases.
        if (!purchase.getCustomerAccount().getId().equals(customer.getId())) {
            throw new IllegalArgumentException(
                    "Purchase was not found for this customer.");
        }

        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment reference is required.");
        }

        String normalizedReference = reference.trim();

        if (purchaseRepository.existsByTransactionReference(normalizedReference)) {
            throw new IllegalStateException(
                    "That payment reference has already been submitted.");
        }

        purchase.submitPaymentReference(normalizedReference);
        purchaseRepository.save(purchase);

        return new PaymentReferenceResponse(
                purchase.getId(),
                purchase.getStatus(),
                purchase.getSubmittedAt());
    }

    /**
     * Safe confirmation fields for a submitted payment reference.
     */
    public record PaymentReferenceResponse(
            Long purchaseId,
            PurchaseStatus status,
            java.time.OffsetDateTime submittedAt) {}  
            
                /**
     * Lists payment submissions that need manual verification.
     */
    @Transactional(readOnly = true)
    public List<PaymentReviewResponse> listPendingReviews() {
        return purchaseRepository
                .findByStatusOrderByCreatedAtAsc(PurchaseStatus.PENDING_REVIEW)
                .stream()
                .map(this::toPaymentReviewResponse)
                .toList();
    }

    /**
     * Approves a payment after the tipster verifies it in the Lipa account.
     */
    @Transactional
    public PaymentReviewResponse approvePayment(Long purchaseId) {
        MkekaPurchase purchase = findPurchase(purchaseId);
        purchase.approve();
        return toPaymentReviewResponse(purchaseRepository.save(purchase));
    }

    /**
     * Rejects a payment reference that the tipster cannot verify.
     */
    @Transactional
    public PaymentReviewResponse rejectPayment(Long purchaseId) {
        MkekaPurchase purchase = findPurchase(purchaseId);
        purchase.reject();
        return toPaymentReviewResponse(purchaseRepository.save(purchase));
    }

    private MkekaPurchase findPurchase(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase was not found."));
    }

    /**
     * Builds a review response while the purchase relationships are available.
     */
    private PaymentReviewResponse toPaymentReviewResponse(MkekaPurchase purchase) {
        return new PaymentReviewResponse(
                purchase.getId(),
                purchase.getBetSlip().getId(),
                purchase.getBetSlip().getTitle(),
                purchase.getCustomerAccount().getPhoneNumber(),
                purchase.getAmountTzs(),
                purchase.getTransactionReference(),
                purchase.getStatus(),
                purchase.getSubmittedAt(),
                purchase.getReviewedAt());
    }

    /**
     * Payment details the tipster needs to verify a submission.
     */
    public record PaymentReviewResponse(
            Long purchaseId,
            Long betSlipId,
            String mkekaTitle,
            String customerPhoneNumber,
            BigDecimal amountTzs,
            String transactionReference,
            PurchaseStatus status,
            java.time.OffsetDateTime submittedAt,
            java.time.OffsetDateTime reviewedAt) {}

                /**
     * Returns payment progress only to the customer who owns the purchase.
     */
    @Transactional(readOnly = true)
    public CustomerPurchaseStatusResponse getCustomerPurchaseStatus(
            String customerPhoneNumber,
            Long purchaseId) {

        CustomerAccount customer = customerAccountRepository
                .findByPhoneNumber(customerPhoneNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer account was not found."));

        MkekaPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase was not found for this customer."));

        if (!purchase.getCustomerAccount().getId().equals(customer.getId())) {
            throw new IllegalArgumentException(
                    "Purchase was not found for this customer.");
        }

        return new CustomerPurchaseStatusResponse(
                purchase.getId(),
                purchase.getBetSlip().getTitle(),
                purchase.getAmountTzs(),
                "TZS",
                purchase.getStatus(),
                purchase.getCreatedAt(),
                purchase.getSubmittedAt(),
                purchase.getReviewedAt());
    }

    /**
     * Payment progress fields shown to the purchase owner.
     */
    public record CustomerPurchaseStatusResponse(
            Long purchaseId,
            String mkekaTitle,
            BigDecimal amountTzs,
            String currencyCode,
            PurchaseStatus status,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime submittedAt,
            java.time.OffsetDateTime reviewedAt) {}
}