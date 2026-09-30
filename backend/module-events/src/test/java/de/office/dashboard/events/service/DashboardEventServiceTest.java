package de.office.dashboard.events.service;

import de.office.dashboard.common.constants.DateTimeFormatterConstants;
import de.office.dashboard.events.dto.DashboardEventResponse;
import de.office.dashboard.events.entity.DashboardEvent;
import de.office.dashboard.events.mapper.DashboardEventMapper;
import de.office.dashboard.events.odooConnection.OdooClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardEventService}.
 * <p>
 * Regression guard for the cached date-range query: the service returned the
 * whole cache (about two weeks of events) for every request that fell inside
 * the cached span, so a two-day window came back with far more events than
 * asked for.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DashboardEventServiceTest {

    @Mock
    private DashboardEventMapper dashboardEventMapper;

    @Mock
    private OdooClient odooClient;

    @InjectMocks
    private DashboardEventService service;

    private Map<String, Object> employeeWithBirthday(LocalDate birthday) {
        return Map.of(
                "id", 1L,
                "name", "Max Mustermann",
                "birthday", birthday.toString(),
                "work_email", "max@example.test");
    }

    private void mapEntitiesLikeProduction() {
        when(dashboardEventMapper.toResponse(any(DashboardEvent.class))).thenAnswer(invocation -> {
            DashboardEvent event = invocation.getArgument(0);
            String formattedDate = event.getDashboardEventDate().format(DateTimeFormatterConstants.DATE_FORMATTER);
            return new DashboardEventResponse(
                    event.getDashboardEventType().name(),
                    event.getEmployeeName(),
                    formattedDate,
                    "Beschreibung",
                    event.getEmployeeEmail());
        });
    }

    @Test
    void getEventsInDateRange_returnsOnlyEventsInsideTheRequestedWindow() throws Exception {
        LocalDate today = LocalDate.now();
        when(odooClient.getEmployees()).thenReturn(List.of(employeeWithBirthday(today.plusDays(2))));
        when(odooClient.getContracts()).thenReturn(List.of());
        mapEntitiesLikeProduction();

        service.refreshEventsDaily(); // fills the cache for today .. today+13

        List<DashboardEventResponse> result = service.getEventsInDateRange(today, today.plusDays(1));

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsInDateRange_keepsEventsInsideTheRequestedWindow() throws Exception {
        LocalDate today = LocalDate.now();
        when(odooClient.getEmployees()).thenReturn(List.of(employeeWithBirthday(today.plusDays(2))));
        when(odooClient.getContracts()).thenReturn(List.of());
        mapEntitiesLikeProduction();

        service.refreshEventsDaily();

        List<DashboardEventResponse> result = service.getEventsInDateRange(today, today.plusDays(2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dashboardEventDate())
                .isEqualTo(today.plusDays(2).format(DateTimeFormatterConstants.DATE_FORMATTER));
    }
}
