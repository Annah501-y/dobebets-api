package com.dobebets.api.prediction;

/**
 * Final result recorded after a prediction is settled.
 */
public enum PredictionOutcome {
    PENDING,
    WON,
    LOST,
    VOID
}