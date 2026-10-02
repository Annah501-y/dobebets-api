package com.dobebets.api.betslip;

import com.dobebets.api.prediction.Prediction;
import com.dobebets.api.prediction.PredictionRepository;
import com.dobebets.api.prediction.PredictionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;

/**
 * Applies business rules when the tipster creates and prepares a bet slip.
 */
@Service
public class BetSlipService {

        private final BetSlipRepository betSlipRepository;
        private final PredictionRepository predictionRepository;

        public BetSlipService(
                        BetSlipRepository betSlipRepository,
                        PredictionRepository predictionRepository) {
                this.betSlipRepository = betSlipRepository;
                this.predictionRepository = predictionRepository;
        }

        /**
         * Creates an unpublished slip and validates its premium price and currency.
         */
        @Transactional
        public BetSlip createDraft(
                        String title,
                        String description,
                        boolean premium,
                        BigDecimal priceAmount,
                        String currencyCode) {

                if (title == null || title.isBlank() || title.trim().length() > 120) {
                        throw new IllegalArgumentException(
                                        "Title is required and must be at most 120 characters.");
                }

                if (description != null && description.length() > 500) {
                        throw new IllegalArgumentException(
                                        "Description must be at most 500 characters.");
                }

                // This tipster's mkeka prices are recorded in Tanzanian shillings.
                if (currencyCode == null || !currencyCode.trim().equalsIgnoreCase("TZS")) {
                        throw new IllegalArgumentException(
                                        "Currency must be TZS (Tanzanian shillings).");
                }

                BigDecimal finalPrice = priceAmount == null
                                ? BigDecimal.ZERO
                                : priceAmount;

                if (finalPrice.signum() < 0
                                || finalPrice.stripTrailingZeros().scale() > 2) {
                        throw new IllegalArgumentException(
                                        "Price must be zero or greater, with no more than two decimal places.");
                }

                if (premium && finalPrice.signum() <= 0) {
                        throw new IllegalArgumentException(
                                        "A premium slip must have a price greater than zero.");
                }

                if (!premium) {
                        finalPrice = BigDecimal.ZERO;
                }

                BetSlip slip = new BetSlip(
                                title.trim(),
                                description == null ? null : description.trim(),
                                premium,
                                finalPrice,
                                "TZS");

                return betSlipRepository.save(slip);
        }

        /**
         * Adds a private draft prediction to a draft slip.
         */
        @Transactional
        public BetSlip addPrediction(Long slipId, Long predictionId) {
                BetSlip slip = betSlipRepository.findById(slipId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No bet slip exists with ID " + slipId + "."));

                if (slip.getStatus() != BetSlipStatus.DRAFT) {
                        throw new IllegalStateException(
                                        "Predictions can only be added to a draft slip.");
                }

                Prediction prediction = predictionRepository.findById(predictionId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No prediction exists with ID " + predictionId + "."));

                // Keep published tips out of slips until premium visibility rules are in place.
                if (prediction.getStatus() != PredictionStatus.DRAFT) {
                        throw new IllegalArgumentException(
                                        "Only draft predictions can be added to a slip at this stage.");
                }

                boolean alreadyAdded = slip.getPredictions().stream()
                                .anyMatch(existing -> existing.getId().equals(predictionId));

                if (alreadyAdded) {
                        throw new IllegalArgumentException(
                                        "That prediction is already on this slip.");
                }

                slip.addPrediction(prediction);
                return betSlipRepository.save(slip);
        }

        /**
         * Publishes a draft mkeka after at least one prediction has been added.
         */
        @Transactional
        public BetSlip publishDraft(Long slipId) {
                BetSlip slip = betSlipRepository.findById(slipId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No bet slip exists with ID " + slipId + "."));

                if (slip.getStatus() != BetSlipStatus.DRAFT) {
                        throw new IllegalStateException(
                                        "Only a draft mkeka can be published.");
                }

                if (slip.getPredictions().isEmpty()) {
                        throw new IllegalStateException(
                                        "Add at least one prediction before publishing the mkeka.");
                }

                slip.publish();
                return betSlipRepository.save(slip);
        }

            /**
     * Retrieves published mkekas for the public catalog.
     */
    @Transactional(readOnly = true)
    public List<BetSlip> listPublished() {
        return betSlipRepository.findByStatusOrderByIdDesc(
                BetSlipStatus.PUBLISHED);
    }
}