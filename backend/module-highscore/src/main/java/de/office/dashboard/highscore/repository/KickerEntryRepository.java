package de.office.dashboard.highscore.repository;

import de.office.dashboard.highscore.entity.KickerEntry;
import de.office.dashboard.highscore.projection.PlayerPointsProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for accessing and querying {@link KickerEntry} data.
 * <p>
 * Provides custom queries for team-based leaderboard calculations and match history retrieval.
 * Uses native SQL for complex team aggregation logic.
 * </p>
 */
public interface KickerEntryRepository extends JpaRepository<KickerEntry, Long> {

    /**
     * Finds the top 3 kicker teams ranked by total wins.
     * <p>
     * This SQL query:
     * <ul>
     *   <li>Combines winning teams from both Team A and Team B</li>
     *   <li>Normalizes team names by alphabetically ordering player names</li>
     *   <li>Groups by team combination to count total wins</li>
     *   <li>Orders by win count (descending) and most recent win date</li>
     *   <li>Limits results to top 3 teams</li>
     * </ul>
     * </p>
     * <p>
     * Note: Uses MySQL-specific GROUP_CONCAT function.
     * </p>
     *
     * @return list of top 3 team projections with normalized team name and win count
     */
    @Query(value = """
            SELECT
                t.team_name AS name,
                COUNT(*) AS totalPoints
            FROM (
                SELECT
                    ke.id,
                    ke.created_at AS win_created_at,
                    GROUP_CONCAT(a.player_name ORDER BY a.player_name SEPARATOR ' + ') AS team_name
                FROM kicker_entry ke
                JOIN kicker_entry_team_a_player a ON a.kicker_entry_id = ke.id
                WHERE ke.match_result = 'TEAM_A_WIN'
                GROUP BY ke.id, ke.created_at

                UNION ALL

                SELECT
                    ke.id,
                    ke.created_at AS win_created_at,
                    GROUP_CONCAT(b.player_name ORDER BY b.player_name SEPARATOR ' + ') AS team_name
                FROM kicker_entry ke
                JOIN kicker_entry_team_b_player b ON b.kicker_entry_id = ke.id
                WHERE ke.match_result = 'TEAM_B_WIN'
                GROUP BY ke.id, ke.created_at
            ) t
            GROUP BY t.team_name
            ORDER BY totalPoints DESC, MAX(t.win_created_at) DESC
            LIMIT 3
            """, nativeQuery = true)
    List<PlayerPointsProjection> findKickerTop3Teams();

    /**
     * Retrieves the most recent kicker matches in chronological order.
     *
     * @param pageable pagination parameters (requesting last N matches)
     * @return list of kicker entries ordered by creation date (newest first)
     */
    @Query("""
                select k
                from KickerEntry k
                order by k.createdAt desc
            """)
    List<KickerEntry> findLastEntries(Pageable pageable);
}

