package de.itestra.dashboard.parking.service;

import de.itestra.dashboard.parking.dto.parkinganalytics.response.BusiestDayOfMonth;
import de.itestra.dashboard.parking.dto.parkinganalytics.response.DailyOccupancy;
import de.itestra.dashboard.parking.dto.parkinganalytics.response.ParkingAnalyticsResponse;
import de.itestra.dashboard.parking.dto.parkinganalytics.response.WeekdayBooking;
import de.itestra.dashboard.parking.entity.ParkingEntry;
import de.itestra.dashboard.parking.repository.ParkingEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for the parking analytics aggregation logic.
 * <p>
 * The repository is mocked; every assertion targets the pure aggregation
 * results (weekday histogram, occupancy ratios, busiest day-of-month).
 * </p>
 */
class ParkingAnalyticsServiceTest {

    private static final int MAX_SPOTS = 5;

    ParkingEntryRepository parkingEntryRepository;
    ParkingAnalyticsService service;

    @BeforeEach
    void setUp() throws Exception {
        parkingEntryRepository = Mockito.mock(ParkingEntryRepository.class);
        service = new ParkingAnalyticsService(parkingEntryRepository, MAX_SPOTS);
    }

    private ParkingEntry entry(String employeeName, LocalDate date) {
        ParkingEntry entry = new ParkingEntry();
        entry.setEmployeeName(employeeName);
        entry.setDate(date);
        return entry;
    }

    @Test
    void weekdayHistogram_coversAllSevenDays_withZeroBuckets() {
        // Monday and Wednesday only - all other weekdays must still be present
        List<ParkingEntry> entries = List.of(
                entry("A", LocalDate.of(2026, 8, 17)), // Monday
                entry("B", LocalDate.of(2026, 8, 19)) // Wednesday
        );

        List<WeekdayBooking> histogram = service.bookingsPerWeekday(entries);

        assertEquals(7, histogram.size());
        assertEquals("MONDAY", histogram.get(0).weekday());
        assertEquals(1, histogram.get(0).bookings());
        assertEquals("TUESDAY", histogram.get(1).weekday());
        assertEquals(0, histogram.get(1).bookings());
        assertEquals("WEDNESDAY", histogram.get(2).weekday());
        assertEquals(1, histogram.get(2).bookings());
        assertEquals("SUNDAY", histogram.get(6).weekday());
        assertEquals(0, histogram.get(6).bookings());
    }

    @Test
    void weekdayHistogram_countsBookingsPerWeekdayAcrossWeeks() {
        List<ParkingEntry> entries = List.of(
                entry("A", LocalDate.of(2026, 8, 3)), // Monday
                entry("B", LocalDate.of(2026, 8, 10)), // Monday
                entry("C", LocalDate.of(2026, 8, 17)), // Monday
                entry("D", LocalDate.of(2026, 8, 7)) // Friday
        );

        List<WeekdayBooking> histogram = service.bookingsPerWeekday(entries);

        assertEquals(3, histogram.get(0).bookings()); // Monday
        assertEquals(1, histogram.get(4).bookings()); // Friday
    }

    @Test
    void occupancyPerDay_isSortedByDate_andRatioIsRoundedToTwoDecimals() {
        List<ParkingEntry> entries = List.of(
                entry("A", LocalDate.of(2026, 8, 20)),
                entry("B", LocalDate.of(2026, 8, 20)),
                entry("C", LocalDate.of(2026, 8, 20)),
                entry("D", LocalDate.of(2026, 8, 21))
        );

        List<DailyOccupancy> occupancy = service.occupancyPerDay(entries);

        assertEquals(2, occupancy.size());
        assertEquals(LocalDate.of(2026, 8, 20), occupancy.get(0).date());
        assertEquals(3, occupancy.get(0).bookedSpots());
        assertEquals(MAX_SPOTS, occupancy.get(0).totalSpots());
        assertEquals(0.6, occupancy.get(0).occupancyRatio());
        assertEquals(LocalDate.of(2026, 8, 21), occupancy.get(1).date());
        assertEquals(0.2, occupancy.get(1).occupancyRatio());
    }

    @Test
    void occupancyPerDay_skipsDaysWithoutBookings() {
        List<ParkingEntry> entries = List.of(entry("A", LocalDate.of(2026, 8, 20)));

        List<DailyOccupancy> occupancy = service.occupancyPerDay(entries);

        assertEquals(1, occupancy.size());
        assertEquals(LocalDate.of(2026, 8, 20), occupancy.get(0).date());
    }

    @Test
    void busiestDayOfMonth_picksHighestCount() {
        List<ParkingEntry> entries = List.of(
                entry("A", LocalDate.of(2026, 8, 3)),
                entry("B", LocalDate.of(2026, 8, 3)),
                entry("C", LocalDate.of(2026, 8, 15)),
                entry("D", LocalDate.of(2026, 9, 15))
        );

        BusiestDayOfMonth busiest = service.busiestDayOfMonth(entries);

        assertEquals(3, busiest.dayOfMonth());
        assertEquals(2, busiest.bookings());
    }

    @Test
    void busiestDayOfMonth_breaksTiesTowardEarliestDay() {
        // Day 10 and day 20 both have two bookings -> day 10 wins deterministically
        List<ParkingEntry> entries = List.of(
                entry("A", LocalDate.of(2026, 8, 10)),
                entry("B", LocalDate.of(2026, 8, 10)),
                entry("C", LocalDate.of(2026, 8, 20)),
                entry("D", LocalDate.of(2026, 8, 20))
        );

        BusiestDayOfMonth busiest = service.busiestDayOfMonth(entries);

        assertEquals(10, busiest.dayOfMonth());
        assertEquals(2, busiest.bookings());
    }

    @Test
    void buildResponse_emptyEntries_yieldsEmptyAggregatesAndNoBusiestDay() {
        ParkingAnalyticsResponse response = service.buildResponse(List.of());

        assertNotNull(response);
        assertEquals(7, response.bookingsPerWeekday().size());
        assertTrue(response.occupancyPerDay().isEmpty());
        assertNull(response.busiestDayOfMonth().dayOfMonth());
        assertEquals(0, response.busiestDayOfMonth().bookings());
        assertEquals(0, response.totalBookings());
        assertEquals(MAX_SPOTS, response.totalSpots());
    }

    @Test
    void computeAnalytics_readsAllEntriesFromRepository() {
        Mockito.when(parkingEntryRepository.findAll())
                .thenReturn(List.of(entry("A", LocalDate.of(2026, 8, 17))));

        ParkingAnalyticsResponse response = service.computeAnalytics();

        assertEquals(1, response.totalBookings());
        Mockito.verify(parkingEntryRepository).findAll();
        verifyNoMoreInteractions(parkingEntryRepository);
    }

    @Test
    void ratio_handlesZeroCapacityWithoutDivisionByZero() {
        ParkingAnalyticsService zeroCapacityService =
                new ParkingAnalyticsService(parkingEntryRepository, 0);

        List<DailyOccupancy> occupancy = zeroCapacityService
                .occupancyPerDay(List.of(entry("A", LocalDate.of(2026, 8, 20))));

        assertEquals(0.0, occupancy.get(0).occupancyRatio());
        assertEquals(0, occupancy.get(0).totalSpots());
    }
}
