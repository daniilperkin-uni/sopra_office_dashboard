package de.office.dashboard.highscore.dto.kicker.request;

import de.office.dashboard.highscore.MatchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object for creating or updating a kicker match entry.
 * <p>
 * Defines teams (1-2 players per team) and match outcome.
 * Validated to ensure proper team sizes and no duplicate players across teams.
 * </p>
 *
 * @param teamAPlayers list of player names in Team A (required, 1-2 players)
 * @param teamBPlayers list of player names in Team B (required, 1-2 players)
 * @param matchResult result of the match indicating which team won (required)
 */
@Schema(description = "DTO to request a kicker entry")
public record KickerEntryRequest(
        @Schema(
                description = "Players of team A",
                example = "[\"Max\", \"Dora\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull @Size(min = 1, max = 2)
        List<@NotBlank String> teamAPlayers,

        @Schema(
                description = "Players of team B",
                example = "[\"Ida\", \"Hans\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull @Size(min = 1, max = 2)
        List<@NotBlank String> teamBPlayers,

        @Schema(description = "Match result", example = "TEAM_A_WIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        MatchResult matchResult
) {
}
