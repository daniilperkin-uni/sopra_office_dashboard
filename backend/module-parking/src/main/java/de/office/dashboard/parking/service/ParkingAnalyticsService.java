package de.office.dashboard.parking.service;

import de.office.dashboard.parking.dto.parkinganalytics.response.BusiestDayOfMonth;
import de.office.dashboard.parking.dto.parkinganalytics.response.DailyOccupancy;
import de.office.dashboard.parking.dto.parkinganalytics.response.ParkingAnalyticsResponse;
import de.office.dashboard.parking.dto.parkinganalytics.response.WeekdayBooking;
import de.office.dashboard.parking.entity.ParkingEntry;
import de.office.dashboard.parking.repository.ParkingEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service for computing parking statistics over existing parking entries.
 * <p>
 * All aggregates are computed on read from the persisted
 * {@link ParkingEntry} rows - no additional schema is involved:
 * <ul>
 * <li>Bookings per weekday histogram (Monday through Sunday, always
 * complete so charts can render zero-height bars)</li>
 * <li>Occupancy per day (booked spots divided by the configured total number
 * of spots) for every day that has at least one booking</li>
 * <li>Busiest day-of-month stat (ties are resolved deterministically in
 * favor of the earliest day-of-month)</li>
 * </ul>
 * </p>
 */
@Service
public class ParkingAnalyticsService {

    private final ParkingEntryRepository parkingEntryRepo;
    private final int maximumParkingSpots;

    /**
     * Constructs a new ParkingAnalyticsService.
     *
     * @param parkingEntryRepo    repository granting read access to parking entries
     * @param maximumParkingSpots configured capacity used as occupancy denominator
     */
    public ParkingAnalyticsService(ParkingEntryRepository parkingEntryRepo,
            @Value("${maximumParkingSpots}") int maximumParkingSpots) {
        this.parkingEntryRepo = parkingEntryRepo;
        this.maximumParkingSpots = maximumParkingSpots;
    }

    /**
     * Computes all parking analytics over all stored parking entries.
     *
     * @return the aggregated analytics response
     */
    public ParkingAnalyticsResponse computeAnalytics() {
        return buildResponse(parkingEntryRepo.findAll());
    }

    /**
     * Pure aggregation over an entry list - package-private so it can be
     * tested without Spring.
     *
     * @param entries the parking entries to aggregate
     * @return the aggregated analytics response
     */
    ParkingAnalyticsResponse buildResponse(List<ParkingEntry> entries) {
        return new ParkingAnalyticsResponse(
                bookingsPerWeekday(entries),
                occupancyPerDay(entries),
                busiestDayOfMonth(entries),
                entries.size(),
                maximumParkingSpots);
    }

    /**
     * Counts bookings for each weekday from Monday to Sunday. Weekdays
     * without bookings are reported with zero so the chart shows a complete
     * week axis.
     *
     * @param entries the parking entries to aggregate
     * @return one histogram bucket per weekday, ordered Monday to Sunday
     */
    List<WeekdayBooking> bookingsPerWeekday(List<ParkingEntry> entries) {
        Map<DayOfWeek, Long> counts = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getDate().getDayOfWeek(),
                        Collectors.counting()));

        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .stream()
                .map(day -> new WeekdayBooking(day.name(), counts.getOrDefault(day, 0L)))
                .toList();
    }

    /**
     * Computes booked/total occupancy for every day that has at least one
     * booking, sorted by date ascending.
     *
     * @param entries the parking entries to aggregate
     * @return one occupancy point per day with bookings
     */
    List<DailyOccupancy> occupancyPerDay(List<ParkingEntry> entries) {
        Map<LocalDate, Long> counts = entries.stream()
                .collect(Collectors.groupingBy(ParkingEntry::getDate, TreeMap::new,
                        Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> new DailyOccupancy(
                        e.getKey(),
                        e.getValue().intValue(),
                        maximumParkingSpots,
                        ratio(e.getValue())))
                .toList();
    }

    /**
     * Finds the day-of-month with the most bookings. Ties resolve to the
     * earliest day-of-month; with no entries the empty result is returned.
     *
     * @param entries the parking entries to aggregate
     * @return the busiest day-of-month stat
     */
    BusiestDayOfMonth busiestDayOfMonth(List<ParkingEntry> entries) {
        if (entries.isEmpty()) {
            return BusiestDayOfMonth.none();
        }
        Map<Integer, Long> countsByDayOfMonth = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getDate().getDayOfMonth(),
                        Collectors.counting()));

        Map.Entry<Integer, Long> best = countsByDayOfMonth.entrySet().stream()
                .min(Comparator.comparing((Map.Entry<Integer, Long> e) -> e.getValue()).reversed()
                        .thenComparing(Map.Entry::getKey))
                .orElseThrow();
        return new BusiestDayOfMonth(best.getKey(), best.getValue());
    }

    /**
     * Computes booked/total as a ratio rounded to two decimal places.
     */
    private double ratio(long bookedSpots) {
        if (maximumParkingSpots <= 0) {
            return 0.0;
        }
        return Math.round(((double) bookedSpots / maximumParkingSpots) * 100.0) / 100.0;
    }
}
