package de.itestra.dashboard.communitylunch.dto.option.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding a custom meal option to an event.
 *
 * @param label custom meal option label (max 120 characters)
 * @param saveToDefaultCatalog whether to save this custom option to the food catalog for future reuse
 */
@Schema(description = "Request to add a custom meal option")
public record AddCustomOptionRequest(
        @Schema(description = "Custom meal option label", example = "Pizza", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 120)
        String label,

        @Schema(description = "Whether to save to food catalog for reuse", example = "true")
        boolean saveToDefaultCatalog
) {
}

