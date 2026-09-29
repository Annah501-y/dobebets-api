package com.dobebets.api.prediction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Database operations for creating and retrieving predictions.
 */
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * Lists predictions for the user-facing feed or tipster dashboard.
     */
    List<Prediction> findByStatusOrderByIdDesc(PredictionStatus status);

    /**
     * Lists all predictions attached to a particular local match.
     */
    List<Prediction> findByMatch_IdOrderByIdAsc(Long matchId);

        /**
     * Loads predictions with their match and league, avoiding extra database queries.
     */
    @Query("""
            select p from Prediction p
            join fetch p.match m
            join fetch m.league l
            where p.status = :status
            order by m.kickoffAt asc
            """)
    List<Prediction> findWithMatchByStatus(
            @Param("status") PredictionStatus status);
}