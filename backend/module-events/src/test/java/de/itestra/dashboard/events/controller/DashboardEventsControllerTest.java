package de.itestra.dashboard.events.controller;

import de.itestra.dashboard.events.dto.DashboardEventResponse;
import de.itestra.dashboard.events.service.DashboardEventService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardEventsController}.
 * <p>
 * Regression guard for the 500 error that {@code GET /api/dashboard-events}
 * returned when it was called without query parameters: the optional start and
 * end dates were passed as null into the service, which dereferenced them. The
 * controller now defaults a missing range to today plus 14 days.
 * </p>
 */
class DashboardEventsControllerTest {

    private final DashboardEventService dashboardEventService = mock(DashboardEventService.class);

    private final DashboardEventsController controller = new DashboardEventsController(dashboardEventService);

    @Test
    void defaultsBothMissingDatesToTheNextFourteenDays() {
        when(dashboardEventService.getEventsInDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        controller.getDashboardEvents(null, null);

        LocalDate today = LocalDate.now();
        verify(dashboardEventService).getEventsInDateRange(today, today.plusDays(14));
    }

    @Test
    void defaultsOnlyTheMissingEndDateRelativeToTheGivenStartDate() {
        when(dashboardEventService.getEventsInDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        LocalDate start = LocalDate.of(2026, 1, 2);
        controller.getDashboardEvents(start, null);

        verify(dashboardEventService).getEventsInDateRange(start, start.plusDays(14));
    }

    @Test
    void keepsAnExplicitlyRequestedDateRange() {
        when(dashboardEventService.getEventsInDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 3);
        controller.getDashboardEvents(start, end);

        verify(dashboardEventService).getEventsInDateRange(start, end);
    }

    @Test
    void returnsTheEventsReportedByTheService() {
        DashboardEventResponse response =
                new DashboardEventResponse("BIRTHDAY", "Max Mustermann", "24.12.2025", "Geburtstag", "max@example.test");
        when(dashboardEventService.getEventsInDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(response));

        var result = controller.getDashboardEvents(null, null);

        assertThat(result).containsExactly(response);
    }
}
