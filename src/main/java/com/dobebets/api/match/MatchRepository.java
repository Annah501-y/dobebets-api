package com.dobebets.api.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<FootballMatch, Long> {
            /**
     * Finds a fixture by its unique API-Football fixture ID.
     */
    Optional<FootballMatch> findByExternalId(String externalId);

    @Query("""
            select m from FootballMatch m join fetch m.league l
            where (:leagueId is null or l.id = :leagueId)
              and m.kickoffAt >= :from and m.kickoffAt < :to
              and (:search = '' or lower(m.homeTeam) like lower(concat('%', :search, '%'))
                   or lower(m.awayTeam) like lower(concat('%', :search, '%')))
            order by m.kickoffAt asc
            """)
    List<FootballMatch> search(@Param("leagueId") Long leagueId,
                              @Param("from") OffsetDateTime from,
                              @Param("to") OffsetDateTime to,
                              @Param("search") String search);
}
