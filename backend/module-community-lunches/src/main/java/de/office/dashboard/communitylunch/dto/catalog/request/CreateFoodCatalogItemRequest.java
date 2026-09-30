package de.office.dashboard.communitylunch.dto.catalog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new food catalog item.
 *
 * @param label food item label (max 120 characters, will be checked for duplicates)
 */
@Schema(description = "Request to create a new food catalog item")
public record CreateFoodCatalogItemRequest(
        @Schema(description = "The label of the food catalog item", example = "Pizza", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String label
) {
}

