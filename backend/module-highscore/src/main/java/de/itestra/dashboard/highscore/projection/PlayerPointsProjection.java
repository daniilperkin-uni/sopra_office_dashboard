package de.itestra.dashboard.highscore.projection;

/**
 * Projection interface for kicker team leaderboard data.
 * <p>
 * Used by repository queries to retrieve team names 
 * and their total win counts.
 * </p>
 */
public interface PlayerPointsProjection {
    /**
     * Gets the normalized team name.
     * <p>
     * Team name format: "Player1 + Player2" with players in alphabetical order.
     * </p>
     *
     * @return team name
     */
    String getName();

    /**
     * Gets the team's total number of wins.
     *
     * @return total win count
     */
    Long getTotalPoints();
}
