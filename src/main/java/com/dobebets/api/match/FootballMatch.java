package com.dobebets.api.match;

import com.dobebets.api.league.League;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(name = "matches", uniqueConstraints = @UniqueConstraint(name = "uk_match_external_id", columnNames = "external_id"))
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "home_team", nullable = false, length = 100)
    private String homeTeam;

    @Column(name = "away_team", nullable = false, length = 100)
    private String awayTeam;

    @Column(name = "kickoff_at", nullable = false)
    private OffsetDateTime kickoffAt;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "external_id", unique = true, length = 100)
    private String externalId;

    protected FootballMatch() {}

    public FootballMatch(League league, String homeTeam, String awayTeam, OffsetDateTime kickoffAt,
                         String status, Integer homeScore, Integer awayScore, String externalId) {
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.externalId = externalId;
    }

        /**
     * Updates a stored fixture with its latest API-Football data.
     * Keeps the local database ID and provider fixture ID unchanged.
     */
        public void updateFromApiFootball(
            League league,
            String homeTeam,
            String awayTeam,
            OffsetDateTime kickoffAt,
            String status,
            Integer homeScore,
            Integer awayScore) {
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public Long getId() { return id; }
    public League getLeague() { return league; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public OffsetDateTime getKickoffAt() { return kickoffAt; }
    public String getStatus() { return status; }
    public Integer getHomeScore() { return homeScore; }
    public Integer getAwayScore() { return awayScore; }
}
