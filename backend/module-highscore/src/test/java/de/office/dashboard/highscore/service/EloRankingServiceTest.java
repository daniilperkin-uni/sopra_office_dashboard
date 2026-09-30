package de.office.dashboard.highscore.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the rankings aggregation in {@link EloRankingService}.
 */
class EloRankingServiceTest {

    private static EloRankingService serviceOf(EloRankingService.DartsEntryView... entries) {
        return new EloRankingService(() -> List.of(entries));
    }

    @Test
    @DisplayName("Consecutive entry pairs form matches; lower darts wins")
    void pairsFormMatches() {
        // Alice finishes in 10, Bob needs 15 -> Alice wins.
        EloRankingService s = serviceOf(
                new EloRankingService.DartsEntryView("Alice", 10),
                new EloRankingService.DartsEntryView("Bob", 15));
        List<EloRankingService.PlayerRanking> rankings = s.getRankings();

        assertEquals(2, rankings.size());
        EloRankingService.PlayerRanking top = rankings.get(0);
        assertEquals("Alice", top.playerName());
        assertTrue(top.elo() > 1000);
        assertEquals(1, top.wins());
        assertEquals(0, top.losses());
        assertEquals(1, top.currentStreak());
        assertEquals(1, top.bestStreak());

        EloRankingService.PlayerRanking second = rankings.get(1);
        assertEquals("Bob", second.playerName());
        assertEquals(0, second.wins());
        assertEquals(1, second.losses());
    }

    @Test
    @DisplayName("Equal finish counts are skipped as ambiguous")
    void equalCountsSkipped() {
        EloRankingService s = serviceOf(
                new EloRankingService.DartsEntryView("A", 12),
                new EloRankingService.DartsEntryView("B", 12));
        List<EloRankingService.PlayerRanking> rankings = s.getRankings();
        assertEquals(0, rankings.size());
    }

    @Test
    @DisplayName("Same player on both sides of a pair is skipped")
    void samePlayerSkipped() {
        EloRankingService s = serviceOf(
                new EloRankingService.DartsEntryView("A", 10),
                new EloRankingService.DartsEntryView("A", 20));
        assertEquals(0, s.getRankings().size());
    }

    @Test
    @DisplayName("Win streaks accumulate and reset on loss")
    void streakTracking() {
        EloRankingService s = serviceOf(
                new EloRankingService.DartsEntryView("W", 5), new EloRankingService.DartsEntryView("L", 50),
                new EloRankingService.DartsEntryView("W", 6), new EloRankingService.DartsEntryView("X", 40),
                new EloRankingService.DartsEntryView("Y", 7), new EloRankingService.DartsEntryView("W", 30));

        EloRankingService.PlayerRanking w = s.getRankings().stream()
                .filter(r -> r.playerName().equals("W")).findFirst().orElseThrow();
        assertEquals(2, w.wins());
        assertEquals(1, w.losses());
        assertEquals(0, w.currentStreak());
        assertEquals(2, w.bestStreak());
    }

    @Test
    @DisplayName("Empty history produces empty table")
    void emptyHistory() {
        assertTrue(serviceOf().getRankings().isEmpty());
    }
}
