package de.itestra.dashboard.parking.controller;

import de.itestra.dashboard.parking.dto.parkingentry.request.ParkingEntryRequest;
import de.itestra.dashboard.parking.dto.parkingentry.response.ParkingEntriesForDayResponse;
import de.itestra.dashboard.parking.dto.parkingentry.response.ParkingEntryResponse;
import de.itestra.dashboard.parking.dto.recurringreservation.request.RecurringReservationRequest;
import de.itestra.dashboard.parking.dto.recurringreservation.response.RecurringReservationResponse;
import de.itestra.dashboard.parking.entity.ParkingEntry;
import de.itestra.dashboard.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

    private static final Logger log = LoggerFactory.getLogger(ParkingController.class);

    private final ParkingService parkingService;

    /**
     * Constructs a new ParkingController.
     *
     * @param parkingService service for managing parking reservations
     */
    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
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

    /**
     * Global exception handler for all exceptions thrown by controller methods.
     * <p>
     * This handler catches all exceptions and converts them into standardized
     * HTTP 400 (Bad Request) responses with a ProblemDetail body containing
     * the exception message. This provides consistent error responses to clients.
     * </p>
     *
     * @param e the exception that was thrown
     * @return response entity with ProblemDetail and HTTP status 400
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleErrors(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}
