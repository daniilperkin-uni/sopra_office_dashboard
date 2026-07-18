package de.itestra.dashboard.parking.dto.parkingentry.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "DTO to request a parking spot")
public record ParkingEntryRequest(
        @Schema(description = "Name of the employee", example = "Max Mustermann", requiredMode = RequiredMode.REQUIRED)
        @NotBlank String employeeName,

        @Schema(description = "Date formated with dd-MM-yyyy", example = "24-02-2026", requiredMode = RequiredMode.REQUIRED)
        @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate date
) {
}





