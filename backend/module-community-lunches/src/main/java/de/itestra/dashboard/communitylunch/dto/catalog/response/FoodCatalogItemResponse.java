package de.itestra.dashboard.communitylunch.dto.catalog.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for a food catalog item.
 *
 * @param id unique catalog item identifier
 * @param label food item label
 * @param active whether item is active
 */
@Schema(description = "Food catalog item")
public record FoodCatalogItemResponse(
        @Schema(description = "Catalog item ID", example = "3")
        Long id,
        @Schema(description = "Food item label", example = "Pizza")
        String label,
        @Schema(description = "Whether item is active", example = "true")
        boolean active
) {
}
