package de.office.dashboard.parking.mapper;

import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntryResponse;
import de.office.dashboard.parking.entity.ParkingEntry;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting {@link ParkingEntry} entities into {@link ParkingEntryResponse} DTOs.
 * <p>
 * This mapper is responsible for transforming internal JPA entities into
 * lightweight response objects that are safe to expose via the API.
 * It deliberately maps only the fields required by clients and hides
 * internal relationships and persistence details.
 * </p>
 * <p>
 * Used primarily by {@link de.office.dashboard.parking.service.ParkingService}
 * when returning parking entry data from create, update, or read operations.
 * </p>
 */
@Component
public class ParkingEntryMapper {

    /**
     * Converts a {@link ParkingEntry} entity into a {@link ParkingEntryResponse}.
     * <p>
     * The resulting DTO contains only the public-facing fields of a parking entry
     * (ID, employee name, and reservation date), ensuring a stable and clean API
     * contract independent of the underlying database model.
     * </p>
     *
     * @param entry the parking entry entity to convert
     * @return response DTO representing the parking entry
     */
    public ParkingEntryResponse toResponse(ParkingEntry entry) {
        return new ParkingEntryResponse(
                entry.getId(),
                entry.getEmployeeName(),
                entry.getDate()
        );
    }
}
