package de.itestra.dashboard.highscore.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the pure ELO math in {@link EloCalculator}.
 */
class EloCalculatorTest {

    @Test
    @DisplayName("New players enter at the start rating of 1000")
    void newPlayerStartRating() {
        EloCalculator calc = new EloCalculator();
        calc.recordMatch("Alice", "Bob");
        Map<String, Integer> ratings = calc.computeRatings();
        // Symmetric start: winner gains exactly K/2 = 16, loser loses 16.
        assertEquals(1016, ratings.get("Alice"));
        assertEquals(984, ratings.get("Bob"));
    }

    @Test
    @DisplayName("Winner gains exactly what the loser loses")
    void zeroSum() {
        EloCalculator calc = new EloCalculator();
        calc.recordMatch("A", "B");
        Map<String, Integer> ratings = calc.computeRatings();
        int winnerGain = ratings.get("A") - EloCalculator.START_RATING;
        int loserLoss = EloCalculator.START_RATING - ratings.get("B");
        assertEquals(winnerGain, loserLoss);
    }

    @Test
    @DisplayName("Beating a much stronger player awards more points")
    void upsetPaysMore() {
        EloCalculator upset = new EloCalculator();
        // Build up Bob as the strong player first.
        upset.recordMatch("Bob", "Carol");
        upset.recordMatch("Bob", "Dave");
        int bobStrong = upset.computeRatings().get("Bob");

        // Fresh calculator with same history plus Alice beating strong Bob.
        EloCalculator withUpset = new EloCalculator();
        withUpset.recordMatch("Bob", "Carol");
        withUpset.recordMatch("Bob", "Dave");
        double bobBefore = withUpset.computeRatings().get("Bob");
        withUpset.recordMatch("Alice", "Bob");
        int aliceGain = withUpset.computeRatings().get("Alice") - EloCalculator.START_RATING;

        // Alice beat a >1000 player, so she earned more than the flat 16.
        assertTrue(aliceGain > EloCalculator.K_FACTOR / 2);
        assertTrue(bobBefore >= bobStrong - 1); // sanity: history replayed identically
    }

    @Test
    @DisplayName("Expected score is symmetric and centered at equal ratings")
    void expectedScoreProperties() {
        assertEquals(0.5, EloCalculator.expectedScore(1000, 1000), 1e-9);
        assertEquals(EloCalculator.expectedScore(1500, 1200),
                1.0 - EloCalculator.expectedScore(1200, 1500), 1e-9);
        assertTrue(EloCalculator.expectedScore(1500, 1000) > 0.5);
    }

    @Test
    @DisplayName("Repeated wins converge toward a stable advantage")
    void convergence() {
        EloCalculator calc = new EloCalculator();
        for (int i = 0; i < 20; i++) {
            calc.recordMatch("Strong", "Weak");
        }
        Map<String, Integer> ratings = calc.computeRatings();
        int gap = ratings.get("Strong") - ratings.get("Weak");
        // After enough one-sided results the system expects Strong to win,
        // so each further win yields ~1 point; the gap stabilizes well below
        // 20 * 32.
        assertTrue(gap > 200);
        assertTrue(gap < 500);
    }

    @Test
    @DisplayName("Blank player names are rejected")
    void blankNamesRejected() {
        EloCalculator calc = new EloCalculator();
        assertThrows(IllegalArgumentException.class, () -> calc.recordMatch("", "Bob"));
        assertThrows(IllegalArgumentException.class, () -> calc.recordMatch("A", null));
    }

    @Test
    @DisplayName("No matches -> empty rating table")
    void emptyHistory() {
        assertTrue(new EloCalculator().computeRatings().isEmpty());
    }
}
