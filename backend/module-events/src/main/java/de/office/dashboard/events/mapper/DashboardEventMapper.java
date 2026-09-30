package de.office.dashboard.events.mapper;

import de.office.dashboard.common.constants.DateTimeFormatterConstants;
import de.office.dashboard.events.dto.DashboardEventResponse;
import de.office.dashboard.events.entity.DashboardEvent;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

/**
 * Mapper component for converting DashboardEvent entities to DashboardEventResponse DTOs.
 * <p>
 * Handles the transformation of entity objects to response objects including formatting
 * dates and building user-friendly event descriptions.
 * </p>
 */
@Component
public class DashboardEventMapper {

    /**
     * Converts a DashboardEvent entity to a DashboardEventResponse DTO.
     *
     * @param dashboardEvent the entity to convert
     * @return the response DTO with formatted data and description
     */
    public DashboardEventResponse toResponse(DashboardEvent dashboardEvent) {
        String dashboardEventType = dashboardEvent.getDashboardEventType().name();
        String employeeName = dashboardEvent.getEmployeeName();
        DateTimeFormatter f = DateTimeFormatterConstants.DATE_FORMATTER;
        String dashboardEventDate = dashboardEvent.getDashboardEventDate().format(f);
        String dashboardEventDescription = buildDescription(dashboardEvent, dashboardEventDate);
        String employeeEmail = dashboardEvent.getEmployeeEmail();

        return new DashboardEventResponse(
                dashboardEventType, employeeName, dashboardEventDate, dashboardEventDescription, employeeEmail
        );
    }

    /**
     * Builds a description for the dashboard event.
     * Generates different messages based on the event type.
     *
     * @param dashboardEvent the event to describe
     * @param date           the formatted date string
     * @return the event description text
     */
    private String buildDescription(DashboardEvent dashboardEvent, String date) {
        return switch (dashboardEvent.getDashboardEventType()) {
            case BIRTHDAY -> dashboardEvent.getEmployeeName() + " hat Geburtstag am " + date + "!";
            case WORK_ANNIVERSARY -> getWorkAnniversaryDesc(dashboardEvent, date);
            case PROBATION_END -> dashboardEvent.getEmployeeName() + " hat Probezeitende am " + date + "!";
        };
    }

    /**
     * Generates a description for work anniversary events.
     * Calculates the number of years employed and formats the message accordingly.
     *
     * @param dashboardEvent the work anniversary event
     * @param date           the formatted date string
     * @return the work anniversary description
     */
    private String getWorkAnniversaryDesc(DashboardEvent dashboardEvent, String date) {
        int originalDate = dashboardEvent.getOriginalEventDate().getYear();
        int dashboardEventDate = dashboardEvent.getDashboardEventDate().getYear();
        // The dashboard date already is the anniversary itself, so the year
        // difference is the number of completed years. Adding one reported a
        // five-year anniversary as "6 Jahre".
        int years = dashboardEventDate - originalDate;
        return String.format(date + " ist %s %d Jahre im Team!",
                dashboardEvent.getEmployeeName(), years);
    }
}
