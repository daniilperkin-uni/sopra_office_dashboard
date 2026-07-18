package de.itestra.dashboard.parking.dto.recurringreservation.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the status of a date when creating or updating recurring reservations.
 * <p>
 * Used in {@link RecurringReservationResponse} to indicate
 * whether a parking spot can be reserved on a particular date, or why it cannot be reserved.
 * This helps users understand which dates in their recurring reservation request were
 * successful and which failed.
 * </p>
 */
@Schema(description = "Status of a date when creating or updating recurring reservations")
public enum DateStatus {

    /**
     * The date is available and a parking spot can be reserved.
     */
    @Schema(description = "The date is available and a parking spot can be reserved")
    OK,

    /**
     * The employee already has a parking reservation for this date.
     */
    @Schema(description = "The employee already has a parking reservation for this date")
    RESERVED_BEFORE,

    /**
     * All parking spots for this date are already occupied.
     */
    @Schema(description = "All parking spots for this date are already occupied")
    FULL
}
