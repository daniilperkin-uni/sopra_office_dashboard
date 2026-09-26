package de.itestra.dashboard.communitylunch.controller;

import de.itestra.dashboard.communitylunch.dto.choice.request.LunchChoiceRequest;
import de.itestra.dashboard.communitylunch.dto.choice.response.LunchChoiceResponse;
import de.itestra.dashboard.communitylunch.dto.choice.response.LunchResultsResponse;
import de.itestra.dashboard.communitylunch.dto.event.request.CreateCommunityLunchEventRequest;
import de.itestra.dashboard.communitylunch.dto.event.request.UpdateCommunityLunchEventRequest;
import de.itestra.dashboard.communitylunch.dto.event.response.CommunityLunchEventResponse;
import de.itestra.dashboard.communitylunch.dto.option.request.AddCustomOptionRequest;
import de.itestra.dashboard.communitylunch.dto.option.request.AddFromCatalogRequest;
import de.itestra.dashboard.communitylunch.entity.LunchStatus;
import de.itestra.dashboard.communitylunch.service.CommunityLunchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing community lunch events and employee meal choices.
 * <p>
 * Provides endpoints for creating and managing community lunch events, adding meal options
 * from the catalog or as custom entries, tracking employee food choices, and viewing voting results.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/api/community-lunches")
@Tag(name = "Community Lunches", description = "Community lunch event and meal choice management")
public class CommunityLunchController {

    private final CommunityLunchService service;

    /**
     * Constructs a new CommunityLunchController.
     *
     * @param service service for managing community lunch events and choices
     */
    public CommunityLunchController(CommunityLunchService service) {
        this.service = service;
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming community lunch events", description = "Retrieves community lunch events scheduled within the next N days from today")
    @ApiResponse(responseCode = "200", description = "List of upcoming events", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CommunityLunchEventResponse.class))))
    public ResponseEntity<List<CommunityLunchEventResponse>> getUpcomingCommunityLunches(
            @Parameter(description = "Number of days to look ahead (1 - 365)", example = "30")
            @RequestParam(name = "days", defaultValue = "30") @Min(1) @Max(365) int days
    ) {
        return ResponseEntity.ok(service.getUpcomingCommunityLunches(days));
    }

    @GetMapping
    @Operation(summary = "Get events in date range", description = "Retrieves all community lunch events between two dates (inclusive). The 'to' date must be on or after the 'from' date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of events in the date range", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CommunityLunchEventResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid date range (to < from)")
    })
    public ResponseEntity<List<CommunityLunchEventResponse>> getCommunityLunchesBetween(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01", required = true)
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-01-31", required = true)
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to
    ) {
        return ResponseEntity.ok(service.getCommunityLunchesBetween(from, to));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID", description = "Retrieves a specific community lunch event with all its meal options")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> getCommunityLunch(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId
    ) {
        return ResponseEntity.ok(service.getCommunityLunch(eventId));
    }

    @PostMapping
    @Operation(summary = "Create new lunch event", description = "Creates a new community lunch event. Events must have unique dates. Optionally includes initial meal options from the catalog. Sends Mattermost notification on creation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or event with this date already exists"),
            @ApiResponse(responseCode = "409", description = "Event with this date already exists")
    })
    public ResponseEntity<CommunityLunchEventResponse> createCommunityLunch(
            @Valid @RequestBody CreateCommunityLunchEventRequest request
    ) {
        return ResponseEntity.ok(service.createCommunityLunch(request));
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update lunch event", description = "Updates an existing community lunch event's details including date, location, note, and status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or date conflict"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> updateCommunityLunch(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody UpdateCommunityLunchEventRequest request
    ) {
        return ResponseEntity.ok(service.updateCommunityLunch(eventId, request));
    }

    @PatchMapping("/{eventId}/status")
    @Operation(summary = "Update event status", description = "Changes the event status (DRAFT, OPEN, or CLOSED). Only OPEN events accept employee votes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> updateCommunityLunchStatus(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Parameter(description = "New status (DRAFT, OPEN, or CLOSED)", example = "OPEN", required = true)
            @RequestParam(name = "status") @NotNull LunchStatus status
    ) {
        return ResponseEntity.ok(service.updateCommunityLunchStatus(eventId, status));
    }

    @PostMapping("/{eventId}/options/from-catalog")
    @Operation(summary = "Add catalog options to event", description = "Adds one or more meal options from the food catalog to the event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Options added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicate option"),
            @ApiResponse(responseCode = "404", description = "Event or catalog item not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> addOptionsFromCatalog(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody AddFromCatalogRequest request
    ) {
        return ResponseEntity.ok(service.addOptionsFromCatalog(eventId, request));
    }

    @PostMapping("/{eventId}/options/custom")
    @Operation(summary = "Add custom option to event", description = "Adds a custom meal option to the event. Optionally saves it to the catalog for future reuse")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Custom option added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> addCustomOption(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody AddCustomOptionRequest request
    ) {
        return ResponseEntity.ok(service.addCustomOption(eventId, request));
    }

    @PatchMapping("/{eventId}/options/{optionId}")
    @Operation(summary = "Toggle option availability", description = "Activates or deactivates a meal option. Inactive options are hidden from voting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Option status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityLunchEventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event or option not found")
    })
    public ResponseEntity<CommunityLunchEventResponse> updateOptionActive(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Parameter(description = "Unique identifier of the meal option", example = "5", required = true)
            @PathVariable("optionId") @Positive Long optionId,
            @Parameter(description = "Whether the option should be active", example = "true", required = true)
            @RequestParam(name = "active") boolean active
    ) {
        return ResponseEntity.ok(service.updateOptionActive(eventId, optionId, active));
    }

    @DeleteMapping("/{eventId}/options/{optionId}")
    @Operation(summary = "Delete meal option", description = "Removes a meal option from the event")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Option deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event or option not found")
    })
    public ResponseEntity<Void> deleteOption(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Parameter(description = "Unique identifier of the meal option", example = "5", required = true)
            @PathVariable("optionId") @Positive Long optionId
    ) {
        service.deleteOption(eventId, optionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete lunch event", description = "Removes a lunch event and all associated options and employee choices")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteCommunityLunch(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId
    ) {
        service.deleteCommunityLunch(eventId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{eventId}/choices")
    @Operation(summary = "Submit employee meal choice", description = "Records or updates an employee's meal choice for the event. Only allowed when event status is OPEN. One choice per employee per event")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Choice submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Event is not open for voting"),
            @ApiResponse(responseCode = "404", description = "Event or option not found")
    })
    public ResponseEntity<Void> choose(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId,
            @Valid @RequestBody LunchChoiceRequest request
    ) {
        service.choose(eventId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/choices")
    @Operation(summary = "Get employee's meal choices", description = "Retrieves all meal choices made by a specific employee for upcoming events (next 365 days)")
    @ApiResponse(responseCode = "200", description = "List of employee's choices", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LunchChoiceResponse.class))))
    public ResponseEntity<List<LunchChoiceResponse>> getChoicesByEmployee(
            @Parameter(description = "Name of the employee", example = "Max Mustermann", required = true)
            @RequestParam("employeeName") String employeeName
    ) {
        return ResponseEntity.ok(service.getChoicesByEmployee(employeeName));
    }

    @GetMapping("/{eventId}/results")
    @Operation(summary = "Get voting results", description = "Retrieves aggregated voting results for the event showing vote count per meal option, sorted by vote count (descending)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voting results", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LunchResultsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<LunchResultsResponse> results(
            @Parameter(description = "Unique identifier of the lunch event", example = "1", required = true)
            @PathVariable("eventId") @Positive Long eventId
    ) {
        return ResponseEntity.ok(service.results(eventId));
    }
}

