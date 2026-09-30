package de.office.dashboard.communitylunch.dto.choice.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for lunch event voting results.
 *
 * @param eventId ID of the lunch event
 * @param results list of vote counts per option, sorted by count (descending)
 */
@Schema(description = "Voting results for a lunch event")
public record LunchResultsResponse(
        @Schema(description = "Event ID", example = "1")
        Long eventId,
        @Schema(description = "Vote counts per option, sorted by count descending")
        List<OptionResult> results
) {
    /**
     * Vote count for a single meal option.
     *
     * @param optionId ID of the meal option
     * @param label label of the meal option
     * @param count number of votes this option received
     */
    @Schema(description = "Vote count for a meal option")
    public record OptionResult(
            @Schema(description = "Option ID", example = "5")
            Long optionId,
            @Schema(description = "Meal option", example = "Pizza")
            String label,
            @Schema(description = "Number of votes", example = "12")
            long count
    ) {
    }
}
