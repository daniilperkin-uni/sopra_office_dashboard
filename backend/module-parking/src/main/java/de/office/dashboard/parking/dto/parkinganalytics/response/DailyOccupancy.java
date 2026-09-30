package de.office.dashboard.parking.dto.parkinganalytics.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * DTO representing the occupancy of the parking lot on a single day.
 * <p>
 * Occupancy is the number of booked spots relative to the configured total
 * number of spots. Used by the parking analytics endpoint to draw the
 * occupancy trend chart.
 * </p>
 */
@Schema(description = "Parking occupancy for one day")
public record DailyOccupancy(
        @Schema(description = "The date this occupancy value refers to", example = "2026-08-20")
        LocalDate date,

        @Schema(description = "Number of booked spots on this date", example = "3")
        int bookedSpots,

        @Schema(description = "Total number of parking spots available per day", example = "5")
        int totalSpots,

        @Schema(description = "Booked spots divided by total spots (0.0 - 1.0)", example = "0.6")
        double occupancyRatio
) {
}
