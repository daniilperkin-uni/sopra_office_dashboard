package de.itestra.dashboard.communitylunch.dto.choice.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for an employee's meal choice.
 *
 * @param eventId ID of the lunch event
 * @param optionId ID of the chosen meal option
 * @param employeeName name of the employee who made the choice
 */
@Schema(description = "Employee meal choice")
public record LunchChoiceResponse(
        @Schema(description = "Event ID", example = "1")
        Long eventId,
        @Schema(description = "Chosen option ID", example = "5")
        Long optionId,
        @Schema(description = "Employee name", example = "Max")
        String employeeName
) {
}
