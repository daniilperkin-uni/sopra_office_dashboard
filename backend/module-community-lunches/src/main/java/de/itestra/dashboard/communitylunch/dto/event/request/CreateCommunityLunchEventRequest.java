package de.itestra.dashboard.communitylunch.dto.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new community lunch event.
 *
 * @param date event date (must be unique - one event per day)
 * @param location event location (max 120 characters)
 * @param note optional event notes (max 1000 characters)
 * @param initialCatalogItemIds optional list of catalog item IDs to add as initial meal options
 */
@Schema(description = "Request to create a new community lunch event")
public record CreateCommunityLunchEventRequest(
        @Schema(description = "Event date (unique)", example = "2026-02-14", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @FutureOrPresent(message = "Das Eventdatum darf nicht in der Vergangenheit liegen")
        LocalDate date,

        @Schema(description = "Event location", example = "Büro", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 120)
        String location,

        @Schema(description = "Optional event notes", example = "Projektabschlussfeier", maxLength = 1000)
        @Size(max = 1000)
        String note,

        @Schema(description = "Optional list of catalog item IDs to add as initial options", example = "[1, 2, 3]")
        List<Long> initialCatalogItemIds
) {
}
