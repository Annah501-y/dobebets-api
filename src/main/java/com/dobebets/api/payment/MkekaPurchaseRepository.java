package com.dobebets.api.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MkekaPurchaseRepository
                extends JpaRepository<MkekaPurchase, Long> {

        /**
         * Finds purchase requests waiting for the tipster to review.
         */
        List<MkekaPurchase> findByStatusOrderByCreatedAtAsc(PurchaseStatus status);

        /**
         * Checks whether this customer already has approved access to this mkeka.
         */
        boolean existsByCustomerAccount_IdAndBetSlip_IdAndStatus(
                        Long customerAccountId,
                        Long betSlipId,
                        PurchaseStatus status);

        /**
         * Prevents the same mobile-money reference from being submitted twice.
         */
        boolean existsByTransactionReference(String transactionReference);

        /**
         * Prevents multiple active or already-approved purchases for the same mkeka.
         */
        boolean existsByCustomerAccount_IdAndBetSlip_IdAndStatusIn(
                        Long customerAccountId,
                        Long betSlipId,
                        List<PurchaseStatus> statuses);
}