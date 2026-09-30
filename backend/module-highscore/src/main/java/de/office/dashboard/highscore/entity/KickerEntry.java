package de.office.dashboard.highscore.entity;

import de.office.dashboard.highscore.MatchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a kicker match result.
 * <p>
 * Tracks kicker matches where each team consists of 1-2 players.
 * Matches are recorded with the players and which team won.
 * Team standings are calculated based on total wins, with player order.
 * </p>
 * <p>
 * <strong>Business Rules:</strong>
 * <ul>
 *   <li>Each team must have 1 or 2 players</li>
 *   <li>Players cannot appear in both teams in the same match</li>
 *   <li>Team combinations are order-independent for leaderboard calculations</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "kicker_entry")
@Schema(description = "Kicker match result entry")
public class KickerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "2")
    private Long id;

    @ElementCollection
    @CollectionTable(
            name = "kicker_entry_team_a_player",
            joinColumns = @JoinColumn(name = "kicker_entry_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_team_a_player_pos",
                    columnNames = {"kicker_entry_id", "player_pos"}
            )
    )
    @OrderColumn(name = "player_pos")
    @Column(name = "player_name", nullable = false)
    @Schema(description = "List of player names in Team A (1-2 players)", example = "[\"Max\", \"Anna\"]")
    private List<String> teamAPlayerNames = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "kicker_entry_team_b_player",
            joinColumns = @JoinColumn(name = "kicker_entry_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_team_b_player_pos",
                    columnNames = {"kicker_entry_id", "player_pos"}
            )
    )
    @OrderColumn(name = "player_pos")
    @Column(name = "player_name", nullable = false)
    @Schema(description = "List of player names in Team B (1-2 players)", example = "[\"John\"]")
    private List<String> teamBPlayerNames = new ArrayList<>();

    @Column(name = "match_result", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Match result", example = "Team A")
    private MatchResult matchResult;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date and time when the match was played", example = "2025-12-24T14:30:00")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public List<String> getTeamAPlayerNames() {
        return teamAPlayerNames;
    }

    public void setTeamAPlayerNames(List<String> teamAPlayerNames) {
        this.teamAPlayerNames = teamAPlayerNames;
    }

    public List<String> getTeamBPlayerNames() {
        return teamBPlayerNames;
    }

    public void setTeamBPlayerNames(List<String> teamBPlayerNames) {
        this.teamBPlayerNames = teamBPlayerNames;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime date) {
        this.createdAt = date;
    }
}
