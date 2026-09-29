package com.dobebets.api.betslip;

import com.dobebets.api.prediction.Prediction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A single-tipster mkeka containing one or more prediction selections.
 */
@Entity
@Table(name = "bet_slips")
public class BetSlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    // Premium access and payment are handled separately from publication.
    @Column(nullable = false)
    private boolean premium = false;

    // Store 0.00 for free slips; the service will validate premium pricing.
    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAmount = BigDecimal.ZERO;

    // ISO currency code, for example "TZS" or "KES".
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BetSlipStatus status = BetSlipStatus.DRAFT;

    // One prediction can be reused in more than one slip if needed.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "bet_slip_predictions",
            joinColumns = @JoinColumn(name = "bet_slip_id"),
            inverseJoinColumns = @JoinColumn(name = "prediction_id"))
    @OrderColumn(name = "selection_order")
    private List<Prediction> predictions = new ArrayList<>();

    protected BetSlip() {
        // Required by JPA when loading slips from PostgreSQL.
    }

    public BetSlip(
            String title,
            String description,
            boolean premium,
            BigDecimal priceAmount,
            String currencyCode) {
        this.title = title;
        this.description = description;
        this.premium = premium;
        this.priceAmount = priceAmount;
        this.currencyCode = currencyCode;
    }

    /**
     * Adds a prediction selection to this slip.
     */
    public void addPrediction(Prediction prediction) {
        predictions.add(prediction);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPremium() {
        return premium;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BetSlipStatus getStatus() {
        return status;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }
}
