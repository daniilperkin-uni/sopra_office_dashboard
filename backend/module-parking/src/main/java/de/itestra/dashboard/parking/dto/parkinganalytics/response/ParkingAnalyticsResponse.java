package de.itestra.dashboard.parking.dto.parkinganalytics.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO aggregating parking statistics for the admin analytics page.
 * <p>
 * Contains everything needed to render the analytics charts in one payload:
 * bookings per weekday, occupancy per day and the busiest day-of-month stat.
 * All aggregates are computed by
 * {@link de.itestra.dashboard.parking.service.ParkingAnalyticsService}
 * from existing {@link de.itestra.dashboard.parking.entity.ParkingEntry} data.
 * </p>
 */
@Schema(description = "Aggregated parking statistics")
public record ParkingAnalyticsResponse(
        @Schema(description = "Bookings per weekday, Monday through Sunday")
        List<WeekdayBooking> bookingsPerWeekday,

        @Schema(description = "Occupancy per day with at least one booking, sorted by date ascending")
        List<DailyOccupancy> occupancyPerDay,

        @Schema(description = "Day of month that accumulated the most bookings")
        BusiestDayOfMonth busiestDayOfMonth,

        @Schema(description = "Total number of bookings in the analyzed period", example = "42")
        long totalBookings,

        @Schema(description = "Total number of parking spots available per day", example = "5")
        int totalSpots
) {
}
