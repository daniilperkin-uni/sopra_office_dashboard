package de.office.dashboard.parking.mapper;

import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntriesForDayResponse;
import de.office.dashboard.parking.entity.ParkingEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper for converting parking entry entities to response DTOs for a specific day.
 * <p>
 * This mapper creates {@link ParkingEntriesForDayResponse} objects by combining a date
 * with its associated parking entries. Used primarily by {@link de.office.dashboard.parking.service.ParkingService}
 * when retrieving parking information for multiple days.
 * </p>
 */
@Component
public class ParkingEntriesForDayMapper {

    @Value("${maximumParkingSpots}")
    private int maximumParkingSpots;

    /**
     * Converts a date and list of parking entries into a response DTO.
     * <p>
     * Creates a response object that groups all parking entries for a specific day,
     * making it easy for clients to display parking information organized by date.
     * The total capacity is included so clients can render occupancy without
     * hardcoding the configured limit.
     * </p>
     *
     * @param day     the date for which entries are being mapped
     * @param entries list of parking entries for the specified day
     * @return response DTO containing the date, its parking entries, and the total capacity
     */
    public ParkingEntriesForDayResponse toResponse(LocalDate day, List<ParkingEntry> entries) {
        return new ParkingEntriesForDayResponse(day, entries, maximumParkingSpots);
    }
}
