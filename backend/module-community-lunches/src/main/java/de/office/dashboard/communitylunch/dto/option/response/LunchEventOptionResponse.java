package de.office.dashboard.communitylunch.dto.option.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for a lunch event meal option.
 *
 * @param id unique option identifier
 * @param label meal option (from catalog or custom)
 * @param active whether option is available for voting
 */
@Schema(description = "Meal option for a lunch event")
public record LunchEventOptionResponse(
        @Schema(description = "Option ID", example = "5")
        Long id,
        @Schema(description = "Meal option", example = "Pizza")
        String label,
        @Schema(description = "Whether option is active", example = "true")
        boolean active
) {
}
