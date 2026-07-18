package de.itestra.dashboard.communitylunch.dto.event.response;

import de.itestra.dashboard.communitylunch.dto.option.response.LunchEventOptionResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a community lunch event.
 *
 * @param id unique event identifier
 * @param date event date
 * @param location event location
 * @param status event status (DRAFT, OPEN, or CLOSED)
 * @param note optional event notes
 * @param updatedAt last update timestamp
 * @param options list of meal options for this event
 */
@Schema(description = "Community lunch event with meal options")
public record CommunityLunchEventResponse(
        @Schema(description = "Event ID", example = "1")
        Long id,
        @Schema(description = "Event date", example = "2026-02-14")
        LocalDate date,
        @Schema(description = "Event location", example = "Büro")
        String location,
        @Schema(description = "Event status", example = "OPEN")
        String status,
        @Schema(description = "Event notes", example = "Projektabschlussfeier")
        String note,
        @Schema(description = "Last update timestamp", example = "2026-01-26T14:20:00")
        LocalDateTime updatedAt,
        @Schema(description = "Meal options for this event")
        List<LunchEventOptionResponse> options
) {
}
