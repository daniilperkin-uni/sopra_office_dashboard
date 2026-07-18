package de.itestra.dashboard.parking.dto.recurringreservation.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object (DTO) for creating or updating recurring parking reservations.
 * <p>
 * This DTO defines a recurring reservation pattern by specifying a date range and the
 * days of the week on which parking spots should be automatically reserved. The system
 * will generate individual parking entries for all matching dates within the specified
 * range.
 * </p>
 *
 * @param employeeName the name of the employee for whom to reserve parking spots (required)
 * @param startDate    the first date of the recurring reservation period (required, format: dd-MM-yyyy)
 * @param endDate      the last date of the recurring reservation period (required, format: dd-MM-yyyy)
 * @param daysOfWeek   set of weekdays on which parking should be reserved (required, must not be empty)
 * @param skipDates    set of specific dates to exclude from the recurring reservation (optional, format: dd-MM-yyyy)
 */
@Schema(description = "DTO to request a recurring parking reservation")
public record RecurringReservationRequest(
        @Schema(description = "Name of the employee", example = "Max Mustermann", requiredMode = RequiredMode.REQUIRED)
        @NotBlank String employeeName,

        @Schema(description = "Date formated with dd-MM-yyyy", example = "24-02-2026", requiredMode = RequiredMode.REQUIRED)
        @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate startDate,

        @Schema(description = "Date formated with dd-MM-yyyy", example = "24-02-2026", requiredMode = RequiredMode.REQUIRED)
        @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate endDate,

        @Schema(description = "Days of week to reserve", example = "[\"MONDAY\", \"TUESDAY\"]", requiredMode = RequiredMode.REQUIRED)
        @NotEmpty Set<DayOfWeek> daysOfWeek,

        @Schema(
                description = "Specific dates within the period that should be skipped (no reservation will be created on these dates).",
                example = "[\"03-02-2026\", \"10-02-2026\"]"
        )
        @JsonFormat(pattern = "dd-MM-yyyy")
        Set<@NotNull LocalDate> skipDates


) {
}





