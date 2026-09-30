package de.office.dashboard.parking.dto.parkinganalytics.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the busiest day-of-month across all parking bookings.
 * <p>
 * The day-of-month that accumulated the most bookings in the analyzed period.
 * On a tie the earliest day-of-month wins so the result stays deterministic.
 * </p>
 */
@Schema(description = "The day of month with the most parking bookings")
public record BusiestDayOfMonth(
        @Schema(description = "Day of month with most bookings (1-31), null if there are no bookings", example = "15")
        Integer dayOfMonth,

        @Schema(description = "Number of bookings on that day of month", example = "9")
        long bookings
) {
    /**
     * Factory for the empty case when no bookings exist at all.
     *
     * @return a result with no day and zero bookings
     */
    public static BusiestDayOfMonth none() {
        return new BusiestDayOfMonth(null, 0);
    }
}
