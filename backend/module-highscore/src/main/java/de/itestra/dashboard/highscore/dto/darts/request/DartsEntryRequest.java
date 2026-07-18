package de.itestra.dashboard.highscore.dto.darts.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating or updating a darts match entry.
 * <p>
 * Contains the player name and number of darts to finish.
 * Lower dart counts indicate better performance.
 * </p>
 *
 * @param playerName name of the player (required)
 * @param dartsToFinish number of darts needed to finish the game (required, minimum 1)
 */
@Schema(description = "DTO to request a darts match entry")
public record DartsEntryRequest(

        @Schema(
                description = "Name of the player",
                example = "Max",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String playerName,

        @Schema(
                description = "Number of darts needed to finish the game",
                example = "18",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Min(value = 1, message = "Darts to finish must be at least 1")
        Integer dartsToFinish

) {
}
