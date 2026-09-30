package de.office.dashboard.parking.service;

import de.office.dashboard.parking.dto.parkingentry.request.ParkingEntryRequest;
import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntriesForDayResponse;
import de.office.dashboard.parking.dto.parkingentry.response.ParkingEntryResponse;
import de.office.dashboard.parking.dto.recurringreservation.request.RecurringReservationRequest;
import de.office.dashboard.parking.dto.recurringreservation.response.DateStatus;
import de.office.dashboard.parking.dto.recurringreservation.response.RecurringReservationResponse;
import de.office.dashboard.parking.entity.ParkingEntry;
import de.office.dashboard.parking.entity.RecurringReservation;
import de.office.dashboard.parking.mapper.ParkingEntriesForDayMapper;
import de.office.dashboard.parking.mapper.ParkingEntryMapper;
import de.office.dashboard.parking.repository.ParkingEntryRepository;
import de.office.dashboard.parking.repository.RecurringReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing parking space reservations.
 * <p>
 * This service provides functionality for:
 * <ul>
 * <li>Creating, updating, and deleting individual parking entries</li>
 * <li>Managing recurring parking reservations with automatic entry
 * generation</li>
 * <li>Validating availability based on maximum parking spots</li>
 * <li>Ensuring employees don't have duplicate reservations</li>
 * <li>Retrieving parking information for working days</li>
 * <li>Automated cleanup of old entries</li>
 * </ul>
 * </p>
 * <p>
 * Business Rules:
 * <ul>
 * <li>Only working days (Monday-Friday) can be reserved</li>
 * <li>Maximum number of parking spots per day is configurable</li>
 * <li>Each employee can have only one reservation per day</li>
 * <li>Past dates cannot be reserved or modified</li>
 * <li>Recurring reservations automatically generate parking entries</li>
 * </ul>
 * </p>
 */
@Service
public class ParkingService {

    private final ParkingEntryRepository parkingEntryRepo;
    private final RecurringReservationRepository recurringReservationRepository;
    private final ParkingEntriesForDayMapper parkingEntriesForDayMapper;
    private final ParkingEntryMapper parkingEntryMapper;

    @Value("${maximumParkingSpots}")
    private int maximumParkingSpots;

    @Value("${numberOfWorkingDays}")
    private int numberOfWorkingDays;

    public ParkingService(ParkingEntryRepository parkingEntryRepo,
            RecurringReservationRepository recurringReservationRepository,
            ParkingEntriesForDayMapper parkingEntriesForDayMapper, ParkingEntryMapper parkingEntryMapper) {
        this.parkingEntryRepo = parkingEntryRepo;
        this.recurringReservationRepository = recurringReservationRepository;
        this.parkingEntriesForDayMapper = parkingEntriesForDayMapper;
        this.parkingEntryMapper = parkingEntryMapper;
    }

    /**
     * Creates a new parking entry for an employee on a specific date.
     * <p>
     * The creation is executed within a transactional context to ensure
     * atomic validation and persistence. A new {@link ParkingEntry} entity
     * is created and persisted, and the generated identifier is included
     * in the returned response DTO.
     * {@code maximumParkingSpots} can be customized via property.
     * </p>
     * <p>
     * Validation rules:
     * <ul>
     * <li>The date must not be in the past</li>
     * <li>The date must be a working day (Monday–Friday)</li>
     * <li>The date must have available parking spots</li>
     * <li>The employee must not already have a reservation on the same date</li>
     * </ul>
     * </p>
     *
     * @param request the request object containing employee name and desired date
     * @return a {@link ParkingEntryResponse} representing the newly created parking
     *         entry
     * @throws IllegalArgumentException if any validation rule is violated
     */

    @Transactional
    public ParkingEntryResponse createEntry(ParkingEntryRequest request) {
        if (request.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("date in past");
        }
        if (!isWorkingDay(request.date())) {
            throw new IllegalArgumentException("Weekends are not bookable");
        }
        String employeeName = request.employeeName();
        LocalDate date = request.date();

        long parkingSpotsRegistered = parkingEntryRepo.countByDate(date);
        if (parkingSpotsRegistered >= maximumParkingSpots) {
            throw new IllegalArgumentException("No free parking spots for this date.");
        }
        if (parkingEntryRepo.existsByEmployeeNameAndDate(employeeName, date)) {
            throw new IllegalArgumentException("This employee has already registered for this date.");
        }

        ParkingEntry entry = new ParkingEntry();
        entry.setEmployeeName(employeeName);
        entry.setDate(date);

        parkingEntryRepo.save(entry);
        return parkingEntryMapper.toResponse(entry);
    }

    /**
     * Updates an existing parking entry with a new employee name and/or date.
     * <p>
     * The update is performed within a transactional context. The parking entry
     * is loaded as a managed entity and modified in place; changes are persisted
     * automatically via JPA dirty checking when the transaction commits.
     * </p>
     * <p>
     * Validation rules:
     * <ul>
     * <li>The new date must not be in the past</li>
     * <li>The new date must be a working day (Monday–Friday)</li>
     * <li>The new date must have available parking spots, excluding this entry</li>
     * <li>The employee must not already have another reservation on the new
     * date</li>
     * </ul>
     * </p>
     *
     * @param id      the ID of the parking entry to update
     * @param request the request object containing the new employee name and date
     * @return a {@link ParkingEntryResponse} representing the updated parking entry
     * @throws NoSuchElementException   if no parking entry with the given ID exists
     * @throws IllegalArgumentException if any validation rule is violated
     */
    @Transactional
    public ParkingEntryResponse updateEntry(Long id, ParkingEntryRequest request) {
        if (request.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("date in past");
        }
        if (!isWorkingDay(request.date())) {
            throw new IllegalArgumentException("Weekends are not bookable");
        }
        ParkingEntry entry = parkingEntryRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Entry not found"));

        var newEmployeeName = request.employeeName();
        var newDate = request.date();

        long parkingSpotsRegistered = parkingEntryRepo.countByDateAndIdNot(newDate, id);
        if (parkingSpotsRegistered >= maximumParkingSpots) {
            throw new IllegalArgumentException("No free parking spots for this date.");
        }
        boolean duplicate = parkingEntryRepo.existsByEmployeeNameAndDateAndIdNot(newEmployeeName, newDate, id);
        if (duplicate) {
            throw new IllegalArgumentException("This employee has already registered for this date.");
        }
        entry.setEmployeeName(newEmployeeName);
        entry.setDate(newDate);
        return parkingEntryMapper.toResponse(entry);
    }

    /**
     * Updates an existing recurring reservation and regenerates associated parking
     * entries.
     * <p>
     * This method deletes all future parking entries (from today onwards)
     * associated with
     * the recurring reservation and regenerates them based on the new
     * configuration.
     * It validates availability for all target dates before making any changes.
     * </p>
     * <p>
     * The update is performed in an all-or-nothing manner: if validation fails for
     * any target date, no changes are persisted and an exception is thrown.
     * </p>
     * <p>
     * Validation includes:
     * <ul>
     * <li>Start date must be before or equal to end date</li>
     * <li>End date must not be in the past</li>
     * <li>All target dates must have available parking spots (excluding existing
     * entries from this recurring reservation)</li>
     * <li>Employee must not have conflicting reservations on target dates</li>
     * <li>Only working days matching the specified days of the week are
     * considered</li>
     * <li>Skip dates are excluded from generation</li>
     * </ul>
     * </p>
     *
     * @param id      the ID of the recurring reservation to update
     * @param request the request containing updated reservation configuration
     * @param dryRun  if true, performs validation only without saving changes
     * @return {@link RecurringReservationResponse} containing status for each date
     * @throws NoSuchElementException   if the recurring reservation doesn't exist
     * @throws IllegalArgumentException if validation fails for any date
     */
    @Transactional
    public RecurringReservationResponse updateRecurringReservation(Long id, RecurringReservationRequest request,
            boolean dryRun) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("startDate > endDate");
        }
        if (request.endDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("endDate in past");
        }
        boolean preview = dryRun;

        RecurringReservation recurringReservation = recurringReservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recurring reservation not found"));

        LocalDate deleteFrom = LocalDate.now();
        LocalDate deleteTo = recurringReservation.getEndDate().isAfter(request.endDate())
                ? recurringReservation.getEndDate()
                : request.endDate();

        String employeeName = request.employeeName();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        Set<DayOfWeek> daysOfWeek = request.daysOfWeek();
        Set<LocalDate> skipDates = request.skipDates() == null ? Set.of() : request.skipDates();

        Set<ParkingEntry> entriesToSave = new HashSet<>();
        Map<LocalDate, DateStatus> dates = new HashMap<>();
        List<LocalDate> failedDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate effectiveStart = startDate.isBefore(today) ? today : startDate;

        for (LocalDate date = effectiveStart; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (skipDates.contains(date)) {
                continue;
            }
            if (!isWorkingDay(date)) {
                continue;
            }
            if (!daysOfWeek.contains(date.getDayOfWeek())) {
                continue;
            }

            long parkingSpotsRegistered = parkingEntryRepo.countByDateExcludingRecurring(date, id);
            if (parkingSpotsRegistered >= maximumParkingSpots) {
                dates.put(date, DateStatus.FULL);
                failedDates.add(date);
                continue;
            }

            boolean reservedBefore = parkingEntryRepo.existsConflictForEmployeeOnDateExcludingRecurring(
                    employeeName, date, recurringReservation.getId());
            if (reservedBefore) {
                dates.put(date, DateStatus.RESERVED_BEFORE);
                failedDates.add(date);
                continue;
            }

            dates.put(date, DateStatus.OK);

            ParkingEntry parkingEntry = new ParkingEntry();
            parkingEntry.setEmployeeName(employeeName);
            parkingEntry.setDate(date);
            parkingEntry.setRecurringReservation(recurringReservation);
            entriesToSave.add(parkingEntry);
        }

        if (!failedDates.isEmpty()) {
            String dateList = failedDates.stream()
                    .map(LocalDate::toString)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Reservation failed for the following dates: " + dateList);
        }

        if (!preview) {
            // Remove entries in the update range from the existing collection to trigger
            // orphan removal
            recurringReservation.getParkingEntries()
                    .removeIf(entry -> !entry.getDate().isBefore(deleteFrom) && !entry.getDate().isAfter(deleteTo));

            // Add the new entries to the existing collection
            recurringReservation.getParkingEntries().addAll(entriesToSave);

            recurringReservation.setEmployeeName(employeeName);
            recurringReservation.setStartDate(startDate);
            recurringReservation.setEndDate(endDate);
            recurringReservation.setDaysOfWeek(daysOfWeek);
            recurringReservation.setSkipDates(skipDates);

            recurringReservationRepository.save(recurringReservation);
        }

        return new RecurringReservationResponse(recurringReservation.getId(), dates);
    }

    /**
     * Creates a new recurring reservation and generates associated parking entries.
     * <p>
     * This method creates a recurring reservation pattern and automatically
     * generates
     * parking entries for all matching dates that pass validation. Dates that fail
     * validation are skipped, allowing partial success.
     * </p>
     * <p>
     * Generation rules:
     * <ul>
     * <li>Entries are generated from start date to end date (or from today if the
     * start date is in the past)</li>
     * <li>Only working days (Monday–Friday) are considered</li>
     * <li>Only dates matching the specified days of the week are included</li>
     * <li>Skip dates are excluded from generation</li>
     * <li>Each date must have available parking spots</li>
     * <li>The employee must not have existing reservations on target dates</li>
     * </ul>
     * </p>
     * <p>
     * The method returns a result containing the status for each processed date:
     * <ul>
     * <li>{@link DateStatus#OK} – Parking entry was created successfully</li>
     * <li>{@link DateStatus#FULL} – Date has no available parking spots</li>
     * <li>{@link DateStatus#RESERVED_BEFORE} – Employee already has a reservation
     * on this date</li>
     * </ul>
     * Only dates with status {@link DateStatus#OK} are persisted when
     * {@code dryRun} is {@code false}.
     * </p>
     *
     * @param request the request containing reservation configuration
     * @param dryRun  if {@code true}, performs validation only without creating or
     *                persisting any entries
     * @return {@link RecurringReservationResponse} containing the reservation ID
     *         (if created) and the status for each processed date
     * @throws IllegalArgumentException if the start date is after the end date or
     *                                  the end date is in the past
     */
    @Transactional
    public RecurringReservationResponse createRecurringReservation(RecurringReservationRequest request,
            boolean dryRun) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("startDate > endDate");
        }
        if (request.endDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("endDate in past");
        }

        RecurringReservation recurringReservation = new RecurringReservation();

        String employeeName = request.employeeName();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        Set<DayOfWeek> daysOfWeek = request.daysOfWeek();
        Set<LocalDate> skipDates = request.skipDates() == null ? Set.of() : request.skipDates();

        Set<ParkingEntry> entriesToSave = new HashSet<>();
        Map<LocalDate, DateStatus> dates = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate effectiveStart = startDate.isBefore(today) ? today : startDate;

        for (LocalDate date = effectiveStart; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (skipDates.contains(date)) {
                continue;
            }
            if (!isWorkingDay(date)) {
                continue;
            }
            if (!daysOfWeek.contains(date.getDayOfWeek())) {
                continue;
            }

            long parkingSpotsRegistered = parkingEntryRepo.countByDate(date);
            if (parkingSpotsRegistered >= maximumParkingSpots) {
                dates.put(date, DateStatus.FULL);
                continue;
            }

            if (parkingEntryRepo.existsByEmployeeNameAndDate(employeeName, date)) {
                dates.put(date, DateStatus.RESERVED_BEFORE);
                continue;
            }

            dates.put(date, DateStatus.OK);

            ParkingEntry parkingEntry = new ParkingEntry();
            parkingEntry.setEmployeeName(employeeName);
            parkingEntry.setDate(date);
            parkingEntry.setRecurringReservation(recurringReservation);
            entriesToSave.add(parkingEntry);
        }

        if (!dryRun) {
            recurringReservation.setParkingEntries(entriesToSave);
            recurringReservation.setEmployeeName(employeeName);
            recurringReservation.setStartDate(startDate);
            recurringReservation.setEndDate(endDate);
            recurringReservation.setDaysOfWeek(daysOfWeek);
            recurringReservation.setSkipDates(skipDates);

            recurringReservationRepository.save(recurringReservation);
            parkingEntryRepo.saveAll(entriesToSave);
        }

        Long id = dryRun ? null : recurringReservation.getId();
        return new RecurringReservationResponse(id, dates);
    }

    /**
     * Deletes an existing parking entry by its unique identifier.
     * <p>
     * The deletion is performed within a transactional context to ensure
     * consistency. If no parking entry with the given ID exists, the method
     * fails fast by throwing an exception and no changes are applied.
     * </p>
     *
     * @param id the unique identifier of the parking entry to delete
     * @throws NoSuchElementException if no parking entry with the given ID exists
     */
    @Transactional
    public void deleteParkingEntry(Long id) {
        if (!parkingEntryRepo.existsById(id)) {
            throw new NoSuchElementException("Parking entry not found");
        }
        parkingEntryRepo.deleteById(id);
    }

    /**
     * Deletes a recurring reservation and all associated parking entries.
     * <p>
     * The deletion is executed within a transactional context. If the specified
     * recurring reservation does not exist, the operation fails fast by throwing
     * an exception and no changes are applied.
     * </p>
     *
     * @param id the unique identifier of the recurring reservation to delete
     * @throws NoSuchElementException if no recurring reservation with the given ID
     *                                exists
     */
    @Transactional
    public void deleteRecurringReservation(Long id) {
        if (!recurringReservationRepository.existsById(id)) {
            throw new NoSuchElementException("Recurring reservation not found");
        }
        recurringReservationRepository.deleteById(id);
    }

    /**
     * Retrieves all parking entries stored in the database.
     *
     * @return a list of all {@link ParkingEntry} objects
     */
    public List<ParkingEntry> getAllEntries() {
        return parkingEntryRepo.findAll();
    }

    /**
     * Returns parking information for the defined in the configuration working days
     * (excluding weekends).
     * Can be customized via {@code numberOfWorkingDays} property.
     * <p>
     * For each working day, the method collects all parking entries and maps them
     * into a response DTO. This is done by fetching all entries for the entire
     * date range in a single query and then processing them in memory.
     *
     * @return a list of {@link ParkingEntriesForDayResponse} objects,
     *         each representing one of the next ten working days
     */
    public List<ParkingEntriesForDayResponse> getInfoForNextDays() {
        LocalDate current = LocalDate.now();
        List<LocalDate> nextConfiguredWorkingDays = new ArrayList<>();

        while (nextConfiguredWorkingDays.size() < numberOfWorkingDays) {
            if (isWorkingDay(current)) {
                nextConfiguredWorkingDays.add(current);
            }
            current = current.plusDays(1);
        }

        LocalDate startDate = nextConfiguredWorkingDays.get(0);
        LocalDate endDate = nextConfiguredWorkingDays.get(nextConfiguredWorkingDays.size() - 1);

        // 2. Fetch all entries for the entire date range in a single query
        List<ParkingEntry> allEntries = parkingEntryRepo.findAllByDateBetween(startDate, endDate);

        // 3. Group the fetched entries by date for efficient lookup
        Map<LocalDate, List<ParkingEntry>> entriesByDate = allEntries.stream()
                .collect(Collectors.groupingBy(ParkingEntry::getDate));

        // 4. Map the working days to the response DTO, using the grouped entries
        return nextConfiguredWorkingDays.stream()
                .map(day -> {
                    List<ParkingEntry> entriesForDay = entriesByDate.getOrDefault(day, new ArrayList<>());
                    return parkingEntriesForDayMapper.toResponse(day, entriesForDay);
                })
                .collect(Collectors.toList());
    }

    /**
     * Deletes all parking entries from the past.
     * <p>
     * This method is called by the
     * {@link de.office.dashboard.parking.scheduler.ParkingScheduler}
     * to automatically clean up old parking entries. It removes entries from the
     * previous day
     * to keep the database clean and maintain only relevant future reservations.
     * </p>
     */
    public void deleteYesterdayEntries() {
        parkingEntryRepo.deleteParkingEntriesWithDateEarlierThan(LocalDate.now());
    }

    /**
     * Checks if a given date is a working day.
     * <p>
     * Working days are Monday through Friday. Weekends (Saturday and Sunday)
     * are not considered working days and cannot be reserved for parking.
     * </p>
     *
     * @param date the date to check
     * @return true if the date is Monday-Friday, false if it's Saturday or Sunday
     */
    private boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY
                && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
