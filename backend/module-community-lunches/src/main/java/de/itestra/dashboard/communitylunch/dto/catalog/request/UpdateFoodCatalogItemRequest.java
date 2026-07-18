package de.itestra.dashboard.communitylunch.dto.catalog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a food catalog item.
 *
 * @param label new food item label (max 120 characters, will be checked for duplicates)
 */
@Schema(description = "Request to rename a food catalog item")
public record UpdateFoodCatalogItemRequest(
        @Schema(description = "New label of the food catalog item", example = "Pizza", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String label
) {
}

