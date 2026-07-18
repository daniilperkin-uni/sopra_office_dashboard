package de.itestra.dashboard.parking.repository;

import de.itestra.dashboard.parking.entity.RecurringReservation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link RecurringReservation} entity operations.
 * <p>
 * Provides standard CRUD operations for managing recurring parking reservations.
 * All custom queries are handled in {@link ParkingEntryRepository} when dealing
 * with the associated parking entries.
 * </p>
 */
public interface RecurringReservationRepository extends JpaRepository<RecurringReservation, Long> {
}
