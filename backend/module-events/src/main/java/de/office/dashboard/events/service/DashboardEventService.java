package de.office.dashboard.events.service;

import de.office.dashboard.common.constants.DateTimeFormatterConstants;
import de.office.dashboard.events.DashboardEventType;
import de.office.dashboard.events.dto.DashboardEventResponse;
import de.office.dashboard.events.entity.DashboardEvent;
import de.office.dashboard.events.mapper.DashboardEventMapper;
import de.office.dashboard.events.odooConnection.OdooClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for managing dashboard events including birthdays, work
 * anniversaries, and probation endings.
 * <p>
 * This service fetches employee and contract data from Odoo, generates events
 * for specified date ranges, and maintains a cache of events for performance
 * optimization. The cache is automatically refreshed daily via a scheduled task.
 * </p>
 */
@Service
public class DashboardEventService {

    private static final Logger log = LoggerFactory.getLogger(DashboardEventService.class);

    private final DashboardEventMapper dashboardEventMapper;
    private final OdooClient odooClient;
    private volatile List<DashboardEventResponse> cachedEvents = new ArrayList<>();
    private volatile LocalDate lastUpdateDate;
    private volatile LocalDate latestCachedDate;
    private volatile LocalDate earliestCachedDate;

    /**
     * Constructs a new DashboardEventService.
     *
     * @param dashboardEventMapper mapper for converting DashboardEvent entities to
     *                             response DTOs
     * @param odooClient           client for communicating with the Odoo ERP system
     */
    public DashboardEventService(DashboardEventMapper dashboardEventMapper, OdooClient odooClient) {
        this.dashboardEventMapper = dashboardEventMapper;
        this.odooClient = odooClient;
    }

    /**
     * Scheduled task that refreshes the event cache.
     * <p>
     * Default schedule: every hour at minute 12.
     * Can be customized via {@code events.cache.refresh.schedule.cron} property.
     * </p>
     * <p>
     * Generates events for the next 14 days starting from today and updates the
     * cache.
     * This ensures that the dashboard always has up-to-date event information.
     * </p>
     */
    @Scheduled(cron = "${events.cache.refresh.schedule.cron:0 12 * * * *}")
    public void refreshEventsDaily() {
        log.info("Starting refresh of the event cache");
        LocalDate today = LocalDate.now();
        generateEvents(today, today.plusDays(13)); // Next 14 days
    }

    /**
     * Returns the cached list of dashboard events.
     * <p>
     * If the cache has not been updated today, it automatically triggers a refresh
     * before returning the events.
     * </p>
     *
     * @return list of cached dashboard event responses
     */
    public List<DashboardEventResponse> getCachedEvents() {
        log.info("Refreshing events: lastUpdateDate: " + lastUpdateDate + " localDate: " + LocalDate.now());
        if (lastUpdateDate == null || !lastUpdateDate.isEqual(LocalDate.now())) {
            refreshEventsDaily(); // Refresh if not updated today
        }
        return cachedEvents;
    }

    /**
     * Returns dashboard events for a specific date range.
     * <p>
     * If the requested range is outside the currently cached date range, this
     * method generates new events for the specified range. Otherwise, it returns the
     * cached events.
     * </p>
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate   the end date of the range (inclusive)
     * @return list of dashboard events within the specified date range
     */
    public List<DashboardEventResponse> getEventsInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Method getEventsInDateRange called with startDate: " + startDate + " endDate " + endDate);
        if (this.earliestCachedDate == null || this.latestCachedDate == null
                || startDate.isBefore(this.earliestCachedDate) || endDate.isAfter(this.latestCachedDate)) {
            return generateEvents(startDate, endDate);
        }
        return filterByDateRange(getCachedEvents(), startDate, endDate);
    }

    /**
     * Restricts already cached events to the requested range.
     * <p>
     * The cache is filled for a wider window (14 days by default) and
     * {@link #getCachedEvents()} returns the whole list, so without this filter
     * a request for two days came back with about two weeks of events.
     * </p>
     *
     * @param events    the cached events
     * @param startDate the start date of the requested range (inclusive)
     * @param endDate   the end date of the requested range (inclusive)
     * @return the events whose dashboard date falls inside the range
     */
    private List<DashboardEventResponse> filterByDateRange(List<DashboardEventResponse> events,
                                                           LocalDate startDate, LocalDate endDate) {
        return events.stream()
                .filter(event -> isWithinRange(event.dashboardEventDate(), startDate, endDate))
                .collect(Collectors.toList());
    }

    private boolean isWithinRange(String formattedDate, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDate date = LocalDate.parse(formattedDate, DateTimeFormatterConstants.DATE_FORMATTER);
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        } catch (DateTimeParseException e) {
            log.warn("Skipping cached event with unparseable date: {}", formattedDate);
            return false;
        }
    }

    /**
     * Generates events by fetching employee and contract data from Odoo.
     * Updates the cache and tracks the date range of cached data.
     *
     * @param startFilterDate the start date for filtering events
     * @param endFilterDate   the end date for filtering events
     * @return list of generated dashboard event responses
     */
    private List<DashboardEventResponse> generateEvents(LocalDate startFilterDate, LocalDate endFilterDate) {
        log.info("Method generateEvents called with startFilterDate: " + startFilterDate + " endFilterDate "
                + endFilterDate);
        List<Map<String, Object>> employees = null;
        List<Map<String, Object>> contracts = null;

        try {
            employees = odooClient.getEmployees();
            contracts = odooClient.getContracts();

            log.info("Successfully fetched {} employees and {} contracts from Odoo",
                    employees.size(), contracts.size());

            calculateEarliestCachedDate(startFilterDate);
            calculateLatestCachedDate(endFilterDate);
            this.lastUpdateDate = LocalDate.now();
        } catch (Exception e) {
            log.error("Unexpected error during Odoo data fetch: {}", e.getMessage(), e);
            return new ArrayList<>();
        }

        List<DashboardEvent> allEvents = new ArrayList<>();
        allEvents.addAll(getBirthdaysInDateRange(startFilterDate, endFilterDate, employees));
        allEvents.addAll(getWorkAnniversariesInDateRange(startFilterDate, endFilterDate, contracts));
        allEvents.addAll(getProbationEndingsInDateRange(startFilterDate, endFilterDate, contracts));

        cachedEvents = allEvents.stream()
                .map(dashboardEventMapper::toResponse)
                .collect(Collectors.toList());

        return cachedEvents;
    }

    private void calculateEarliestCachedDate(LocalDate startFilterDate) {
        if (this.earliestCachedDate == null) {
            this.earliestCachedDate = startFilterDate;
        } else if (this.earliestCachedDate.isAfter(startFilterDate)) {
            this.earliestCachedDate = startFilterDate;
        }
    }

    private void calculateLatestCachedDate(LocalDate endFilterDate) {
        // Keep the furthest end date: the cache bound must grow with every
        // generated range. Comparing with isAfter() kept the minimum instead,
        // so the bound never extended and later requests re-generated needlessly.
        if (this.latestCachedDate == null || this.latestCachedDate.isBefore(endFilterDate)) {
            this.latestCachedDate = endFilterDate;
        }
    }

    /**
     * Extracts birthday events from employee data within the specified date range.
     * Handles year transitions to ensure birthdays are found across calendar years.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @param employees list of employee data from Odoo
     * @return list of birthday dashboard events
     */
    private List<DashboardEvent> getBirthdaysInDateRange(LocalDate startDate, LocalDate endDate,
                                                         List<Map<String, Object>> employees) {
        List<DashboardEvent> birthdays = new ArrayList<>();

        for (Map<String, Object> employee : employees) {
            try {
                // Odoo returns false (Boolean) for empty fields instead of null
                Object birthdayObj = employee.get("birthday");
                if (birthdayObj == null || birthdayObj instanceof Boolean) {
                    continue; // Skip employees without birthday
                }

                LocalDate birthday = LocalDate.parse((String) birthdayObj);

                Object emailObj = employee.get("work_email");
                if (emailObj == null || emailObj instanceof Boolean) {
                    continue; // Skip employees without email
                }
                String employeeEmail = (String) emailObj;

                // Determine the range of years to check
                int startYear = startDate.getYear();
                int endYear = endDate.getYear();

                for (int year = startYear - 1; year <= endYear + 1; year++) {
                    LocalDate currentYearBirthday = birthday.withYear(year);

                    if (!currentYearBirthday.isBefore(startDate) && !currentYearBirthday.isAfter(endDate)) {
                        birthdays.add(createDashboardEvent(employee, employeeEmail, DashboardEventType.BIRTHDAY,
                                currentYearBirthday,
                                birthday));
                    }
                }
            } catch (Exception e) {
                log.error("Skipping event due to parsing error: {}", e.getMessage(), e);
                continue;
            }
        }
        return birthdays;
    }

    /**
     * Extracts work anniversary events from contract data within the specified date
     * range.
     * Handles year transitions to find anniversaries across calendar years.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @param contracts list of contract data from Odoo
     * @return list of work anniversary dashboard events
     */
    private List<DashboardEvent> getWorkAnniversariesInDateRange(LocalDate startDate, LocalDate endDate,
                                                                 List<Map<String, Object>> contracts) {
        List<DashboardEvent> workAnniversaries = new ArrayList<>();

        for (Map<String, Object> contract : contracts) {
            try {
                // Odoo returns false (Boolean) for empty fields instead of null
                Object dateStartObj = contract.get("date_start");
                if (dateStartObj == null || dateStartObj instanceof Boolean) {
                    continue; // Skip contracts without start date
                }

                LocalDate contractStartDate = LocalDate.parse((String) dateStartObj);

                // Check if employee_id is valid (Odoo returns false for empty relations)
                Object employeeIdObj = contract.get("employee_id");
                if (employeeIdObj == null || employeeIdObj instanceof Boolean) {
                    continue; // Skip contracts without employee_id
                }

                // Determine the range of years to check
                int startYear = startDate.getYear();
                int endYear = endDate.getYear();

                for (int year = startYear - 1; year <= endYear + 1; year++) {
                    LocalDate currentYearAnniversary = contractStartDate.withYear(year);

                    if (!currentYearAnniversary.isBefore(startDate) && !currentYearAnniversary.isAfter(endDate)) {
                        Object[] employeeIdArray = (Object[]) employeeIdObj;
                        Long employeeId = ((Number) employeeIdArray[0]).longValue();
                        String employeeName = (String) employeeIdArray[1];

                        DashboardEvent dashboardEvent = createDashboardEvent(
                                Map.of("id", employeeId, "name", employeeName),
                                null,
                                DashboardEventType.WORK_ANNIVERSARY,
                                currentYearAnniversary,
                                contractStartDate);
                        workAnniversaries.add(dashboardEvent);
                    }
                }
            } catch (Exception e) {
                log.error("Skipping event due to parsing error: {}", e.getMessage(), e);
                continue;
            }
        }
        return workAnniversaries;
    }

    /**
     * Extracts probation ending events from contract data within the specified date
     * range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @param contracts list of contract data from Odoo
     * @return list of probation ending dashboard events
     */
    private List<DashboardEvent> getProbationEndingsInDateRange(LocalDate startDate, LocalDate endDate,
                                                                List<Map<String, Object>> contracts) {
        List<DashboardEvent> probationEndings = new ArrayList<>();
        for (Map<String, Object> contract : contracts) {
            try {
                Object raw = contract.get("trial_date_end");
                if (raw == null || raw.equals(false)) {
                    continue;
                }
                LocalDate probationEnd = LocalDate.parse((String) raw);

                if (!probationEnd.isBefore(startDate) && !probationEnd.isAfter(endDate)) {
                    // Check if employee_id is valid (Odoo returns false for empty relations)
                    Object employeeIdObj = contract.get("employee_id");
                    if (employeeIdObj == null || employeeIdObj instanceof Boolean) {
                        continue; // Skip contracts without employee_id
                    }

                    DashboardEvent dashboardEvent = new DashboardEvent();
                    Object[] employeeIdArray = (Object[]) employeeIdObj;
                    Long employeeId = ((Number) employeeIdArray[0]).longValue();
                    String employeeName = (String) employeeIdArray[1];

                    dashboardEvent.setEmployeeId(employeeId);
                    dashboardEvent.setEmployeeName(employeeName);
                    dashboardEvent.setDashboardEventType(DashboardEventType.PROBATION_END);
                    dashboardEvent.setDashboardEventDate(probationEnd);
                    dashboardEvent.setOriginalEventDate(probationEnd);
                    probationEndings.add(dashboardEvent);
                }
            } catch (Exception e) {
                log.debug("Skipping event due to parsing error: {}", e.getMessage());
                continue;
            }
        }
        return probationEndings;
    }

    /**
     * Factory method to create a DashboardEvent from employee data.
     *
     * @param employee      map containing employee data (id, name)
     * @param employeeEmail the employee's work email
     * @param type          the type of dashboard event
     * @param eventDate     the date when the event occurs
     * @param originalDate  the original date of the event (e.g., actual birthday)
     * @return a new DashboardEvent instance
     */
    private DashboardEvent createDashboardEvent(Map<String, Object> employee, String employeeEmail,
                                                DashboardEventType type, LocalDate eventDate, LocalDate originalDate) {
        return new DashboardEvent(
                ((Number) employee.get("id")).longValue(),
                (String) employee.get("name"),
                employeeEmail,
                type,
                eventDate,
                originalDate);
    }
}
