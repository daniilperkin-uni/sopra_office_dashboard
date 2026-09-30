package de.office.dashboard.highscore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the outcome of a kicker match.
 * <p>
 * Indicates which team won the match.
 * </p>
 */
@Schema(description = "Result of a kicker match indicating which team won")
public enum MatchResult {
    /** Team A won the match */
    @Schema(description = "Team A won the match")
    TEAM_A_WIN,

    /** Team B won the match */
    @Schema(description = "Team B won the match")
    TEAM_B_WIN
}
