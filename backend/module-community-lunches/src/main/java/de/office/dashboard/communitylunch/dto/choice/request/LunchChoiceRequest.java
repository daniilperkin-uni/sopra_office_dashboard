package de.office.dashboard.communitylunch.dto.choice.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting an employee's meal choice.
 *
 * @param optionId ID of the chosen meal option (must be positive)
 * @param employeeName name of the employee making the choice 
 */
@Schema(description = "Request to submit an employee's meal choice")
public record LunchChoiceRequest(
        @Schema(description = "ID of the chosen meal option", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive
        Long optionId,

        @Schema(description = "Employee name", example = "Max", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 255)
        String employeeName

) {
}

