package de.itestra.dashboard.events.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for dashboard event responses.
 * <p>
 * Represents a dashboard event in API responses with formatted data.
 * This record is returned by the dashboard events API.
 * </p>
 *
 * @param dashboardEventType        the type of event (BIRTHDAY, WORK_ANNIVERSARY, PROBATION_END)
 * @param employeeName              the name of the employee associated with the event
 * @param dashboardEventDate        the formatted date of the event
 * @param dashboardEventDescription a description of the event
 * @param employeeEmail             the email address of the employee (only used if dashboardEventType = BIRTHDAY)
 */
public record DashboardEventResponse(

        @Schema(description = "Type of dashboard event", example = "BIRTHDAY")
        String dashboardEventType,

        @Schema(description = "Name of the employee", example = "Max Mustermann")
        String employeeName,

        @Schema(description = "Date of the dashboard event", example = "24.12.2025")
        String dashboardEventDate,

        @Schema(description = "Detailed description", example = "Max Mustermann's birthday")
        String dashboardEventDescription,

        @Schema(description = "Email of the employee", example = "mustermann@itestra.de")
        String employeeEmail
) {
}

