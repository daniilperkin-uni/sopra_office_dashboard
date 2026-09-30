package de.office.dashboard.highscore.dto.kicker.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Data Transfer Object representing a kicker match entry in API responses.
 * <p>
 * Contains the match details including team compositions, result,
 * and formatted timestamp for display purposes.
 * </p>
 *
 * @param id unique identifier of the kicker entry
 * @param teamAPlayers list of player names in Team A
 * @param teamBPlayers list of player names in Team B
 * @param matchResult textual representation of which team won
 * @param dateText formatted date and time of the match
 */
@Schema(description = "Kicker match entry response")
public record KickerEntryResponse(

        @Schema(
                description = "Unique identifier of the kicker entry",
                example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long id,

        @Schema(
                description = "Players of team A",
                example = "[\"Max\", \"Dora\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<String> teamAPlayers,

        @Schema(
                description = "Players of team B",
                example = "[\"Ida\", \"Hans\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<String> teamBPlayers,

        @Schema(
                description = "Match result",
                example = "Team A won",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String matchResult,

        @Schema(
                description = "Date of the match",
                example = "21:06 25.04.2026"
        )
        String dateText
) {
}
