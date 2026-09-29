package com.dobebets.api.football;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Makes authenticated requests to API-Football from the backend.
 * The API key stays on the server and is never sent to the Flutter app.
 */
@Component
public class ApiFootballClient {

    private final RestClient restClient;

    public ApiFootballClient(
            RestClient.Builder restClientBuilder,
            @Value("${api-football.base-url}") String baseUrl,
            @Value("${api-football.api-key}") String apiKey) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-apisports-key", apiKey)
                .build();
    }

    /**
     * Retrieves competitions that API-Football marks as current.
     */
    public JsonNode getCurrentLeagues() {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/leagues")
                        .queryParam("current", true)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException(
                    "API-Football returned an empty response for current leagues.");
        }

        return response;
    }
        /**
     * Retrieves fixtures scheduled for a specific calendar date in UTC.
     *
     * @param date the date to request from API-Football
     * @return the provider's JSON response, including its fixture array
     */
    public JsonNode getFixturesByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Fixture date must not be null.");
        }

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("date", date.toString())
                        .queryParam("timezone", "UTC")
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException(
                    "API-Football returned an empty response for fixtures.");
        }

        return response;
    }
}