package de.itestra.dashboard.communitylunch.dto.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO for updating an existing community lunch event.
 *
 * @param date new event date (must be unique if changed)
 * @param location new event location (max 120 characters)
 * @param note new event notes (max 1000 characters)
 * @param status new event status (DRAFT, OPEN, CLOSED, or CANCELLED)
 */
@Schema(description = "Request to update a community lunch event")
public record UpdateCommunityLunchEventRequest(
        @Schema(description = "Event date", example = "2026-02-14", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDate date,

        @Schema(description = "Event location", example = "Büro", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 120)
        String location,

        @Schema(description = "Event notes", example = "Projektabschlussfeier", maxLength = 1000)
        @Size(max = 1000)
        String note,

        @Schema(description = "Event status", example = "OPEN", allowableValues = {"DRAFT", "OPEN", "CLOSED", "CANCELLED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(
                regexp = "DRAFT|OPEN|CLOSED|CANCELLED",
                message = "status must be one of: DRAFT, OPEN, CLOSED, CANCELLED"
        )
        String status
) {
}
