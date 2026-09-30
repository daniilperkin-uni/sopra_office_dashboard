package de.office.dashboard.parking.controller;

import de.office.dashboard.parking.dto.parkinganalytics.response.ParkingAnalyticsResponse;
import de.office.dashboard.parking.dto.parkingentry.request.ParkingEntryRequest;
import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntriesForDayResponse;
import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntryResponse;
import de.office.dashboard.parking.dto.recurringreservation.request.RecurringReservationRequest;
import de.office.dashboard.parking.dto.recurringreservation.response.RecurringReservationResponse;
import de.office.dashboard.parking.entity.ParkingEntry;
import de.office.dashboard.parking.service.ParkingAnalyticsService;
import de.office.dashboard.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing parking spot reservations.
 * <p>
 * Provides endpoints to create, retrieve, update, and delete parking spot
 * reservations,
 * including support for recurring reservations. Also provides overview
 * information for upcoming working days.
 * </p>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Parking", description = "Parkingspot reservation modul")
public class ParkingController {

    private final ParkingService parkingService;
    private final ParkingAnalyticsService parkingAnalyticsService;

    /**
     * Constructs a new ParkingController.
     *
     * @param parkingService service for managing parking reservations
     * @param parkingAnalyticsService service computing aggregated parking statistics
     */
    public ParkingController(ParkingService parkingService, ParkingAnalyticsService parkingAnalyticsService) {
        this.parkingService = parkingService;
        this.parkingAnalyticsService = parkingAnalyticsService;
    }

    @GetMapping("/entries")
    @Operation(summary = "Retrieve reserved parking spots", description = "Returns all reserved parking spots")
    @ApiResponse(responseCode = "200", description = "List of reserved parking spots", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParkingEntry.class))))
    public List<ParkingEntry> getEntries() {
        return parkingService.getAllEntries();
    }

    @GetMapping("/overview")
    @Operation(summary = "Retrieve overview of reserved parking spots for the next 2 weeks", description = "Returns a list of reserved parking spots for the next 10 working days")
    @ApiResponse(responseCode = "200", description = "Returns a list of reserved parking spots for the next 10 working days", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParkingEntriesForDayResponse.class))))
    public List<ParkingEntriesForDayResponse> getOverview() {
        return parkingService.getInfoForNextDays();
    }

    /**
     * Returns aggregated parking statistics over all stored entries.
     * <p>
     * The payload feeds the admin analytics page: bookings per weekday
     * histogram, occupancy ratio per day and the busiest day-of-month stat.
     * All aggregates are computed on read by
     * {@link de.office.dashboard.parking.service.ParkingAnalyticsService}.
     * </p>
     *
     * @return the aggregated parking analytics
     */
    @GetMapping("/parking/analytics")
    @Operation(summary = "Retrieve aggregated parking statistics", description = "Returns bookings per weekday, occupancy per day and the busiest day-of-month over all stored parking entries")
    @ApiResponse(responseCode = "200", description = "Aggregated parking analytics", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingAnalyticsResponse.class)))
    public ParkingAnalyticsResponse getAnalytics() {
        return parkingAnalyticsService.computeAnalytics();
    }

    /**
     * Creates a new parking entry for a given employee and date.
     *
     * @param request the request body containing employee name and desired date
     * @return the newly created {@link ParkingEntry}
     * @throws IllegalArgumentException if the date is fully booked or the employee
     *                                  is already registered
     */

    @PostMapping("/entries")
    @Operation(summary = "Create a new parking reservation", description = "Creates a new parking spot reservation for an employee on a specific date."
            +
            " The request will fail if the requested date is already fully booked.")
    @ApiResponse(responseCode = "201", description = "Parking reservation successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingEntry.class)))
    public ResponseEntity<ParkingEntryResponse> createEntry(
            @Valid @RequestBody ParkingEntryRequest request) {
        ParkingEntryResponse saved = parkingService.createEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Updates a parking reservation", description = "Updates the parking spot reservation with the given id. "
            +
            "Fails if the new date is fully booked or the employee already has a reservation on that date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parking reservation successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingEntry.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Parking reservation not found")
    })
    public ResponseEntity<ParkingEntryResponse> updateEntry(
            @Parameter(description = "Unique identifier of the parking entry to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ParkingEntryRequest request) {
        ParkingEntryResponse updated = parkingService.updateEntry(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/recurring")
    @Operation(summary = "Create recurring parking reservation (preview or apply)", description = "Creates a recurring reservation. "
            +
            "Use preview=true to simulate without saving; preview=false to create and save.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preview calculated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecurringReservationResponse.class))),
            @ApiResponse(responseCode = "201", description = "Recurring reservation successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecurringReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business rule violation")
    })
    public ResponseEntity<RecurringReservationResponse> createRecurringReservation(
            @Parameter(description = "If true, simulates the reservation without saving; if false, creates and saves the reservation", example = "false")
            @RequestParam(name = "preview", defaultValue = "false") boolean preview,
            @Valid @RequestBody RecurringReservationRequest request) {

        RecurringReservationResponse result = parkingService.createRecurringReservation(request, preview);
        return ResponseEntity.status(preview ? HttpStatus.OK : HttpStatus.CREATED).body(result);
    }

    @PutMapping("/recurring/{id}")
    @Operation(summary = "Update recurring parking reservation (preview or apply)", description = "Updates an existing recurring reservation. "
            +
            "Use preview=true to simulate without saving; preview=false to apply changes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update/preview calculated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecurringReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Recurring reservation not found")
    })
    public ResponseEntity<RecurringReservationResponse> updateRecurringReservation(
            @Parameter(description = "Unique identifier of the recurring reservation to update", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "If true, simulates the update without saving; if false, applies the changes", example = "false")
            @RequestParam(name = "preview", defaultValue = "false") boolean preview,
            @Valid @RequestBody RecurringReservationRequest request) {
        RecurringReservationResponse result = parkingService.updateRecurringReservation(id, request, preview);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete a parking reservation", description = "Removes an existing parking reservation by its unique identifier. The parking spot will become available for other employees to book.")
    public ResponseEntity<Void> deleteEntry(
            @Parameter(description = "Unique identifier of the parking entry to delete", example = "1", required = true)
            @PathVariable("id") Long id) {
        parkingService.deleteParkingEntry(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/recurring/{id}")
    @Operation(summary = "Delete a recurring reservation", description = "Removes a recurring reservation and all associated parking entries.")
    public ResponseEntity<Void> deleteRecurringReservation(
            @Parameter(description = "Unique identifier of the recurring reservation to delete", example = "1", required = true)
            @PathVariable("id") Long id) {
        parkingService.deleteRecurringReservation(id);
        return ResponseEntity.noContent().build();
    }
}
