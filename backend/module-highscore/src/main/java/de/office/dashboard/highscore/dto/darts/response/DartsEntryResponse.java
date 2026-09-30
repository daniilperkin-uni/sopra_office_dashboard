package de.office.dashboard.highscore.dto.darts.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object representing a darts match entry in API responses.
 * <p>
 * Contains the match result with player name, performance score,
 * and formatted date for display purposes.
 * </p>
 *
 * @param id unique identifier of the darts entry
 * @param player name of the player
 * @param points number of darts needed to finish
 * @param date formatted date and time of the match
 */
@Schema(description = "Darts match entry response")
public record DartsEntryResponse(
        @Schema(
                description = "Unique identifier of the darts entry",
                example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long id,

        @Schema(description = "Name of the employee", example = "Max", requiredMode = Schema.RequiredMode.REQUIRED)
        String player,

        @Schema(description = "Points", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        int points,

        @Schema(description = "Date of the match", example = "21:09 25.04.2026")
        String date
) {
}
