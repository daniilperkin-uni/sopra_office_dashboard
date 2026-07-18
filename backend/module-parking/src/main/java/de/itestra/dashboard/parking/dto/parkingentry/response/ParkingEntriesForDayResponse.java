package de.itestra.dashboard.parking.dto.parkingentry.response;

import de.itestra.dashboard.parking.entity.ParkingEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO to return the reserved parking spots per day")
public record ParkingEntriesForDayResponse(
        @Schema(description = "The date for which parking reservations are shown", example = "2025-01-15")
        LocalDate date,

        @Schema(description = "List of all parking reservations for this date")
        List<ParkingEntry> entries
) {
}

