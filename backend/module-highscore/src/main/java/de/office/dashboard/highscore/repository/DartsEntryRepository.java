package de.office.dashboard.highscore.repository;

import de.office.dashboard.highscore.entity.DartsEntry;
import de.office.dashboard.highscore.projection.DartsPlayerProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for accessing and querying {@link DartsEntry} data.
 * <p>
 * Provides custom queries for leaderboard calculations and match history retrieval.
 * </p>
 */
public interface DartsEntryRepository extends JpaRepository<DartsEntry, Long> {

    /**
     * Finds the top darts players ranked by their best performance (fewest darts to finish).
     * <p>
     * Groups entries by player name and selects the minimum darts count for each player.
     * Results are ordered by fewest darts (ascending), with tie-breaking by most recent match.
     * </p>
     *
     * @param pageable pagination parameters (requesting top N players)
     * @return list of player projections with name and best dart count
     */
    @Query("""
                select d.playerName as name,
                       min(d.dartsToFinish) as totalThrows
                from DartsEntry d
                group by d.playerName
                order by min(d.dartsToFinish) asc, max(d.createdAt) desc
            """)
    List<DartsPlayerProjection> findTop(Pageable pageable);

    /**
     * Retrieves the most recent darts matches in chronological order.
     *
     * @param pageable pagination parameters (requesting last N matches)
     * @return list of darts entries ordered by creation date (newest first)
     */
    @Query("""
                select d
                from DartsEntry d
                order by d.createdAt desc
            """)
    List<DartsEntry> findLastMatches(Pageable pageable);
}
