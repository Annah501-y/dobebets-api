package com.dobebets.api.league;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    List<League> findByActiveTrueOrderByNameAsc();
    Optional<League>findByApiFootballId(Long apiFootballId);
}
