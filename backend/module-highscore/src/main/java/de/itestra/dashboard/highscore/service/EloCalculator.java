package de.itestra.dashboard.highscore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure ELO rating calculation for the darts leaderboard.
 *
 * <p>
 * Standard Elo with K=32 and a start rating of 1000. Matches must be fed in
 * chronological order; each match updates both players symmetrically
 * (winner gains exactly what the loser loses). Kept free of any Spring or
 * JPA dependency so the math is trivially unit-testable.
 * </p>
 */
public final class EloCalculator {

    /** Standard chess K-factor, chosen for fast convergence in a small office league. */
    public static final int K_FACTOR = 32;

    /** Rating every player starts with. */
    public static final int START_RATING = 1000;

    private final List<Match> matches = new ArrayList<>();

    private record Match(String winner, String loser) {
    }

    /**
     * Records one finished match. Order of calls matters: ratings are
     * computed by replaying matches in insertion order.
     *
     * @param winner player name as stored on the winning entry
     * @param loser  player name as stored on the losing entry
     */
    public void recordMatch(String winner, String loser) {
        if (winner == null || loser == null || winner.isBlank() || loser.isBlank()) {
            throw new IllegalArgumentException("Player names must not be blank.");
        }
        matches.add(new Match(winner.trim(), loser.trim()));
    }

    /**
     * Computes current ratings for all players seen in the recorded matches.
     *
     * @return unmodifiable map player name -> elo rating (sorted descending)
     */
    public Map<String, Integer> computeRatings() {
        Map<String, Double> ratings = new HashMap<>();
        for (Match m : matches) {
            double rWinner = ratings.computeIfAbsent(m.winner(), k -> (double) START_RATING);
            double rLoser = ratings.computeIfAbsent(m.loser(), k -> (double) START_RATING);
            double expectedWinner = expectedScore(rWinner, rLoser);
            ratings.put(m.winner(), rWinner + K_FACTOR * (1 - expectedWinner));
            ratings.put(m.loser(), rLoser - K_FACTOR * (1 - expectedWinner));
        }

        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Double> e : ratings.entrySet()) {
            result.put(e.getKey(), (int) Math.round(e.getValue()));
        }
        return result;
    }

    /**
     * Expected score of player A against player B per the Elo formula.
     *
     * @return value between 0 and 1
     */
    public static double expectedScore(double ratingA, double ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }
}
