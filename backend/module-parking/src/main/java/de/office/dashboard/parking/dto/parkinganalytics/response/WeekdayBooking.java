package de.office.dashboard.parking.dto.parkinganalytics.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the number of parking bookings for a single weekday.
 * <p>
 * Used by the parking analytics endpoint to feed the "bookings per weekday"
 * histogram on the admin statistics page.
 * </p>
 */
@Schema(description = "Number of parking bookings aggregated for one weekday")
public record WeekdayBooking(
        @Schema(description = "Name of the weekday", example = "MONDAY")
        String weekday,

        @Schema(description = "Number of bookings on this weekday", example = "12")
        long bookings
) {
}
