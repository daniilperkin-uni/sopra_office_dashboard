package de.itestra.dashboard.highscore.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes per-player darts statistics (ELO rating, win/loss record, and
 * streaks) by replaying the stored match history.
 *
 * <p>
 * Everything is computed on read from existing darts rows - no schema change.
 * Not a Spring component: it is a pure computation class fed through the
 * {@link DartsEntryReader} interface so the aggregation stays unit-testable
 * and is only wired into Spring once an endpoint consumes it.
 * </p>
 */
public class EloRankingService {

    private final DartsEntryReader reader;

    /**
     * Minimal read interface so the ranking logic stays testable without a
     * database.
     */
    public interface DartsEntryReader {

        /**
         * Returns all darts entries in insertion order (oldest first).
         *
         * @return list of entries
         */
        List<DartsEntryView> findAllOrdered();
    }

    /**
     * Structural view of a darts entry: player name and darts needed to
     * finish.
     */
    public record DartsEntryView(String playerName, int dartsToFinish) {
    }

    /**
     * One row of the rankings table.
     */
    public record PlayerRanking(
            String playerName,
            int elo,
            int wins,
            int losses,
            int currentStreak,
            int bestStreak) {
    }

    public EloRankingService(DartsEntryReader reader) {
        this.reader = reader;
    }

    /**
     * Computes the full rankings table, sorted by ELO descending.
     *
     * @return one entry per player seen in the match history
     */
    public List<PlayerRanking> getRankings() {
        EloCalculator calculator = new EloCalculator();
        Map<String, int[]> records = new HashMap<>();
        // records: [0]=wins, [1]=losses, [2]=currentStreak, [3]=bestStreak

        List<DartsEntryView> entries = reader.findAllOrdered();
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                // Pair entries with identical finish counts played "together"
                // is not derivable from the flat schema; instead each entry
                // is compared against the next entry of another player with
                // the same dartsToFinish semantics is ambiguous. We therefore
                // treat consecutive pairs (2i, 2i+1) as one match.
            }
        }

        List<DartsEntryView> ordered = new ArrayList<>(entries);
        for (int i = 0; i + 1 < ordered.size(); i += 2) {
            DartsEntryView first = ordered.get(i);
            DartsEntryView second = ordered.get(i + 1);
            if (!first.playerName().equals(second.playerName())
                    && first.dartsToFinish() != second.dartsToFinish()) {
                boolean firstWins = first.dartsToFinish() < second.dartsToFinish();
                String winner = firstWins ? first.playerName() : second.playerName();
                String loser = firstWins ? second.playerName() : first.playerName();
                calculator.recordMatch(winner, loser);
                applyResult(records, winner, true);
                applyResult(records, loser, false);
            }
        }

        Map<String, Integer> ratings = calculator.computeRatings();
        List<PlayerRanking> rankings = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ratings.entrySet()) {
            int[] r = records.getOrDefault(e.getKey(), new int[4]);
            rankings.add(new PlayerRanking(e.getKey(), e.getValue(), r[0], r[1], r[2], r[3]));
        }
        rankings.sort(Comparator.comparingInt(PlayerRanking::elo).reversed());
        return rankings;
    }

    private void applyResult(Map<String, int[]> records, String player, boolean won) {
        int[] r = records.computeIfAbsent(player, k -> new int[4]);
        if (won) {
            r[0]++;
            r[2] = Math.max(1, r[2] + 1);
            r[3] = Math.max(r[3], r[2]);
        } else {
            r[1]++;
            r[2] = 0;
        }
    }
}
