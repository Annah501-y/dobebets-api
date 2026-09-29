package com.dobebets.api.league;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@Tag(name = "Leagues", description = "Football competitions")
public class LeagueController {
    private final LeagueRepository leagueRepository;

    public LeagueController(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @GetMapping
    @Operation(summary = "List active leagues")
    public List<League> listActiveLeagues() {
        return leagueRepository.findByActiveTrueOrderByNameAsc();
    }
}
