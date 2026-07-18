package de.itestra.dashboard.events.controller;

import de.itestra.dashboard.events.dto.DashboardEventResponse;
import de.itestra.dashboard.events.service.DashboardEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing dashboard events.
 * <p>
 * Provides endpoints to retrieve dashboard events such as birthdays, work anniversaries,
 * and probation endings for specified date ranges.
 * </p>
 */
@RestController
@Tag(name = "Events", description = "Dashboard Events module")
@RequestMapping("/api")
public class DashboardEventsController {

    private final DashboardEventService dashboardEventService;

    /**
     * Constructs a new DashboardEventsController.
     *
     * @param dashboardEventService service for managing dashboard events
     */
    public DashboardEventsController(DashboardEventService dashboardEventService) {
        this.dashboardEventService = dashboardEventService;
    }

    @GetMapping("/dashboard-events")
    @Operation(
            summary = "Retrieve dashboard events within a specified date range or next 14 days",
            description = "Returns birthdays, work anniversaries and probation ends that will occur within the specified date range. If no dates are provided, returns events for the next 14 days."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the list of dashboard events",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = DashboardEventResponse.class)
                    )
            )
    )
    public List<DashboardEventResponse> getDashboardEvents(
            @Parameter(
                    description = "Start date of the range in ISO format (yyyy-MM-dd). If not provided, defaults to today's date.",
                    example = "2026-01-02",
                    schema = @Schema(type = "string", format = "date")
            )
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    description = "End date of the range in ISO format (yyyy-MM-dd). If not provided, defaults to 14 days from the start date.",
                    example = "2026-01-16",
                    schema = @Schema(type = "string", format = "date")
            )
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return dashboardEventService.getEventsInDateRange(startDate, endDate);
    }
}
