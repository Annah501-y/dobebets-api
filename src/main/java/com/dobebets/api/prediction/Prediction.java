package com.dobebets.api.prediction;

import com.dobebets.api.match.FootballMatch;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * A single prediction published by the platform's one tipster.
 */
@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Every prediction must refer to a match in our local fixtures database.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private FootballMatch match;

    // Store enum names so database values stay readable.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PredictionMarket market;

    // Examples: "HOME", "Over 2.5", "Yes", or "2-1".
    @Column(nullable = false, length = 60)
    private String selection;

    // Decimal odds supplied by the tipster for this selection.
    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal odds;

    // Drafts are hidden from users until the tipster publishes them.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PredictionStatus status = PredictionStatus.DRAFT;

    // A new prediction has no settled result yet.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PredictionOutcome outcome = PredictionOutcome.PENDING;

    protected Prediction() {
        // Required by JPA when it loads prediction rows from PostgreSQL.
    }

    public Prediction(
            FootballMatch match,
            PredictionMarket market,
            String selection,
            BigDecimal odds) {
        this.match = match;
        this.market = market;
        this.selection = selection;
        this.odds = odds;
    }

    /**
     * Publishes a draft prediction for the user-facing feed.
     */
    public void publish() {
        if (status != PredictionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft predictions can be published.");
        }

        this.status = PredictionStatus.PUBLISHED;
    }

    public Long getId() {
        return id;
    }

    public FootballMatch getMatch() {
        return match;
    }

    public PredictionMarket getMarket() {
        return market;
    }

    public String getSelection() {
        return selection;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public PredictionStatus getStatus() {
        return status;
    }

    public PredictionOutcome getOutcome() {
        return outcome;
    }
}
