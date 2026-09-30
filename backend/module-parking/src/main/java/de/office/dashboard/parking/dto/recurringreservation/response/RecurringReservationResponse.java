package de.office.dashboard.parking.dto.recurringreservation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

/**
 * Response DTO representing the outcome of creating or updating a recurring parking reservation.
 * <p>
 * Includes the unique identifier of the recurring reservation and a per-date status map.
 * Each date in the map indicates whether a parking entry was created (OK) or why it could not
 * be created (e.g., the day was already reserved by the employee or the parking capacity was full).
 * </p>
 *
 * @param id    unique identifier of the recurring reservation
 * @param dates map of dates to their reservation status ({@link DateStatus#OK}, {@link DateStatus#RESERVED_BEFORE}, or {@link DateStatus#FULL})
 */
@Schema(
        name = "RecurringReservationResponse",
        description = "Outcome of creating or updating a recurring reservation, including the reservation id and a per-date status map"
)
public record RecurringReservationResponse(
        @Schema(
                description = "Unique identifier of the recurring reservation",
                example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long id,

        @Schema(
                description = "Map of dates to their reservation status (OK, RESERVED_BEFORE, or FULL). Keys are ISO-8601 dates (yyyy-MM-dd).",
                example = """
                        {
                          "2026-02-24": "OK",
                          "2026-02-25": "FULL",
                          "2026-02-26": "RESERVED_BEFORE"
                        }
                        """
        )
        Map<LocalDate, DateStatus> dates
) {
}
