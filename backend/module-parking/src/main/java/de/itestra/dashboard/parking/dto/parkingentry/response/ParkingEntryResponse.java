package de.itestra.dashboard.parking.dto.parkingentry.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO representing a parking reservation in API responses")
public record ParkingEntryResponse(
        @Schema(description = "Unique identifier of the parking reservation", example = "1")
        Long id,

        @Schema(description = "Name of the employee who made the reservation", example = "Max Mustermann")
        String employeeName,

        @Schema(description = "Date of the parking reservation", example = "2025-01-15")
        LocalDate date
) {
}
