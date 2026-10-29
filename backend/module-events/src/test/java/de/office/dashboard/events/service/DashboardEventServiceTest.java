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
        return employeeWithBirthday("Max Mustermann", 1L, birthday);
    }

    private Map<String, Object> employeeWithBirthday(String name, long id, LocalDate birthday) {
        return Map.of(
                "id", id,
                "name", name,
                "birthday", birthday.toString(),
                "work_email", name.toLowerCase() + "@example.test");
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

    @Test
    void getEventsInDateRange_keepsEarlierEventsWhenTheWindowGrows() throws Exception {
        LocalDate today = LocalDate.of(2026, 3, 1);
        when(odooClient.getEmployees()).thenReturn(List.of(employeeWithBirthday("Alice", 1L, today.plusDays(1))));
        when(odooClient.getContracts()).thenReturn(List.of());
        mapEntitiesLikeProduction();

        assertThat(service.getEventsInDateRange(today, today.plusDays(5)))
                .extracting(DashboardEventResponse::employeeName)
                .containsExactly("Alice");

        when(odooClient.getEmployees()).thenReturn(List.of(
                employeeWithBirthday("Alice", 1L, today.plusDays(1)),
                employeeWithBirthday("Bob", 2L, today.plusDays(10))));

        assertThat(service.getEventsInDateRange(today.plusDays(6), today.plusDays(15)))
                .extracting(DashboardEventResponse::employeeName)
                .containsExactly("Bob");

        // The second request widened the tracked bounds to today .. today+15, so this
        // one is served from the cache. Alice's birthday must still be in it.
        assertThat(service.getEventsInDateRange(today, today.plusDays(5)))
                .extracting(DashboardEventResponse::employeeName)
                .containsExactly("Alice");
    }
}
