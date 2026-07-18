package de.itestra.dashboard.highscore.dto.matchhistory.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a unified match history entry from either Darts or Kicker games.
 * <p>
 * This DTO combines match data from both game types into a common format,
 * allowing for a chronological display of all recent gaming activity.
 * </p>
 *
 * @param type game type: "Darts" or "Kicker"
 * @param players description of participating players
 * @param points darts count for Darts or points for Kicker
 * @param matchResult outcome description (relevant for Kicker matches)
 * @param date timestamp of when the match was played
 * @param dateText formatted date and time for display
 */
@Schema(description = "Unified match history entry for both game types")
public record MatchHistoryResponse(

        @Schema(description = "Type of the match", example = "Darts", requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(description = "Name of the players", example = "Max and Noah vs Robin and Michael", requiredMode = Schema.RequiredMode.REQUIRED)
        String players,

        @Schema(description = "Points", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer points,

        @Schema(description = "Match result", example = "Team A won", requiredMode = Schema.RequiredMode.REQUIRED)
        String matchResult,

        @Schema(description = "Date of the match", example = "24.04.2026")
        LocalDateTime date,

        @Schema(description = "Date of the match as a text", example = "12:30 24.04.2026")
        String dateText
) {
}
