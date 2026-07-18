package de.itestra.dashboard.parking.repository;

import de.itestra.dashboard.parking.entity.ParkingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for {@link ParkingEntry} entity operations.
 * <p>
 * Provides CRUD operations and custom query methods for managing parking
 * space reservations. Includes specialized queries for validation and
 * conflict detection when updating recurring reservations.
 * </p>
 */
public interface ParkingEntryRepository extends JpaRepository<ParkingEntry, Long> {

    List<ParkingEntry> findAllByDate(LocalDate date);

    List<ParkingEntry> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByEmployeeNameAndDate(String employeeName, LocalDate date);

    long countByDate(LocalDate date);

    void deleteAllByDate(LocalDate date);

    boolean existsByEmployeeNameAndDateAndIdNot(String employeeName, LocalDate date, Long id);

    long countByDateAndIdNot(LocalDate date, Long id);

    void deleteAllByRecurringReservationIdAndDateBetween(Long id, LocalDate from, LocalDate to);

    /**
     * Counts parking entries for a specific date, excluding entries from a given
     * recurring reservation.
     * <p>
     * This method is used when updating a recurring reservation to check
     * availability.
     * It counts all parking entries for the date except those belonging to the
     * recurring reservation being updated, allowing the system to validate if
     * there's parking spot for the updated reservation.
     * </p>
     *
     * @param date        the date to count entries for
     * @param recurringId the ID of the recurring reservation to exclude from the
     *                    count
     * @return number of parking entries for the date, excluding the specified
     * recurring reservation
     */
    @Query("""
                select count(pe)
                from ParkingEntry pe
                where pe.date = :date
                  and (pe.recurringReservation is null or pe.recurringReservation.id <> :recurringId)
            """)
    long countByDateExcludingRecurring(@Param("date") LocalDate date,
                                       @Param("recurringId") Long recurringId);

    /**
     * Checks if an employee has a conflicting reservation on a date, excluding a
     * specific recurring reservation.
     * <p>
     * This method is used when updating a recurring reservation to detect
     * conflicts.
     * It checks if the employee already has a parking entry for the date, excluding
     * entries from the recurring reservation being updated. This prevents false
     * positives when an employee's existing recurring reservation is being
     * modified.
     * </p>
     *
     * @param employeeName the name of the employee to check for
     * @param date         the date to check for conflicts
     * @param recurringId  the ID of the recurring reservation to exclude from the
     *                     check
     * @return true if there's a conflicting entry (not from the specified recurring
     * reservation), false otherwise
     */
    @Query("""
                select (count(pe) > 0)
                from ParkingEntry pe
                where pe.employeeName = :employeeName
                  and pe.date = :date
                  and (pe.recurringReservation is null or pe.recurringReservation.id <> :recurringId)
            """)
    boolean existsConflictForEmployeeOnDateExcludingRecurring(@Param("employeeName") String employeeName,
                                                              @Param("date") LocalDate date,
                                                              @Param("recurringId") Long recurringId);

    @Modifying
    @Query("""
                delete
                from ParkingEntry pe
                where pe.date < :date
            """)
    void deleteParkingEntriesWithDateEarlierThan(@Param("date") LocalDate date);                                  
}
