package com.dobebets.api.payment;

import com.dobebets.api.account.CustomerAccount;
import com.dobebets.api.account.CustomerAccountRepository;
import com.dobebets.api.betslip.BetSlip;
import com.dobebets.api.betslip.BetSlipRepository;
import com.dobebets.api.betslip.BetSlipStatus;
import com.dobebets.api.league.League;
import com.dobebets.api.match.FootballMatch;
import com.dobebets.api.prediction.Prediction;
import com.dobebets.api.prediction.PredictionMarket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controls access to published mkeka selections.
 */
@Service
public class MkekaAccessService {

    private final BetSlipRepository betSlipRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final MkekaPurchaseRepository purchaseRepository;

    public MkekaAccessService(
            BetSlipRepository betSlipRepository,
            CustomerAccountRepository customerAccountRepository,
            MkekaPurchaseRepository purchaseRepository) {
        this.betSlipRepository = betSlipRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Returns selections only when the customer is entitled to view the mkeka.
     */
    @Transactional(readOnly = true)
    public PurchasedMkekaResponse getSelections(
            String customerPhoneNumber,
            Long betSlipId) {

        CustomerAccount customer = customerAccountRepository
                .findByPhoneNumber(customerPhoneNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer account was not found."));

        BetSlip betSlip = betSlipRepository.findById(betSlipId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mkeka was not found."));

        if (betSlip.getStatus() != BetSlipStatus.PUBLISHED) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Published mkeka was not found.");
        }

        boolean hasApprovedPurchase = purchaseRepository
                .existsByCustomerAccount_IdAndBetSlip_IdAndStatus(
                        customer.getId(),
                        betSlip.getId(),
                        PurchaseStatus.APPROVED);

        if (betSlip.isPremium() && !hasApprovedPurchase) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Payment must be approved before viewing this premium mkeka.");
        }

        List<SelectionResponse> selections = betSlip.getPredictions()
                .stream()
                .map(this::toSelectionResponse)
                .toList();

        return new PurchasedMkekaResponse(
                betSlip.getId(),
                betSlip.getTitle(),
                betSlip.getDescription(),
                betSlip.getPriceAmount(),
                betSlip.getCurrencyCode(),
                selections);
    }

    /**
     * Maps a prediction and its fixture into a safe customer-facing response.
     */
    private SelectionResponse toSelectionResponse(Prediction prediction) {
        FootballMatch match = prediction.getMatch();
        League league = match.getLeague();

        return new SelectionResponse(
                prediction.getId(),
                match.getId(),
                league.getName(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getKickoffAt(),
                prediction.getMarket(),
                prediction.getSelection(),
                prediction.getOdds());
    }

    public record PurchasedMkekaResponse(
            Long id,
            String title,
            String description,
            BigDecimal priceAmount,
            String currencyCode,
            List<SelectionResponse> selections) {}

    public record SelectionResponse(
            Long predictionId,
            Long matchId,
            String leagueName,
            String homeTeam,
            String awayTeam,
            OffsetDateTime kickoffAt,
            PredictionMarket market,
            String selection,
            BigDecimal odds) {}
}