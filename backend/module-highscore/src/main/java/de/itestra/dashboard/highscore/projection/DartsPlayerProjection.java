package de.itestra.dashboard.highscore.projection;

/**
 * Projection interface for darts player leaderboard data.
 * <p>
 * Used by repository queries to retrieve player names and their best performance
 * (minimum darts to finish) without loading full entity objects.
 * </p>
 */
public interface DartsPlayerProjection {
    /**
     * Gets the player's name.
     *
     * @return player name
     */
    String getName();

    /**
     * Gets the player's best performance (fewest darts to finish).
     *
     * @return minimum darts count
     */
    Integer getTotalThrows();
}
