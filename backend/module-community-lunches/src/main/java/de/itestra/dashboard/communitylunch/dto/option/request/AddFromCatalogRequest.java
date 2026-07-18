package de.itestra.dashboard.communitylunch.dto.option.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request DTO for adding meal options from the food catalog to an event.
 *
 * @param catalogItemIds list of catalog item IDs to add (must not be empty, all IDs must be positive)
 */
@Schema(description = "Request to add meal options from the food catalog")
public record AddFromCatalogRequest(
        @Schema(description = "List of catalog item IDs to add as meal options", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        List<@NotNull @Positive Long> catalogItemIds
) {
}

