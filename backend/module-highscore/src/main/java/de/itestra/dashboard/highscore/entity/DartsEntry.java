package de.itestra.dashboard.highscore.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a single darts game result.
 * <p>
 * Tracks darts performances by recording how many darts a player needed to 
 * finish a game from a given starting score (e.g. 310 points).
 * Lower dart counts indicate better performance and are ranked higher in the
 * leaderboard.
 * </p>
 * <p>
 * This entity is used for:
 * <ul>
 *   <li>Recording match results</li>
 *   <li>Calculating leaderboard rankings (top players by fewest darts)</li>
 *   <li>Displaying match history</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "darts_entry")
@Schema(description = "Darts game result entry")
public class DartsEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "2")
    private Long id;

    @Column(name = "player_name", nullable = false)
    @Schema(description = "Name of the employee", example = "Max")
    private String playerName;

    @Column(name = "darts_to_finish", nullable = false)
    @Schema(description = "Number of darts needed to finish the game", example = "18")
    private int dartsToFinish;

    @Column(name = "start_score", nullable = false)
    @Schema(description = "Starting score of the darts game", example = "310")
    private int startScore = 310;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date and time when the darts entry was created",
            example = "2025-12-24T12:30:00")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getDartsToFinish() {
        return dartsToFinish;
    }

    public void setDartsToFinish(int dartsToFinish) {
        this.dartsToFinish = dartsToFinish;
    }

    public int getStartScore() {
        return startScore;
    }

    public void setStartScore(int startScore) {
        this.startScore = startScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime date) {
        this.createdAt = date;
    }
}
