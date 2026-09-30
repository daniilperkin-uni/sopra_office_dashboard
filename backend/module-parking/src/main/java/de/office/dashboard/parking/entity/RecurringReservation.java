package de.office.dashboard.parking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a recurring parking space reservation pattern.
 * <p>
 * A recurring reservation automatically creates {@link ParkingEntry} instances
 * for specified days of the week within a date range. This allows employees to
 * reserve parking spots on a regular schedule (e.g., every Monday and Wednesday)
 * without creating individual entries manually.
 * </p>
 * <p>
 * The system automatically generates parking entries based on:
 * <ul>
 *   <li>Start and end date range</li>
 *   <li>Selected days of the week (e.g., MONDAY, WEDNESDAY, FRIDAY)</li>
 *   <li>Skip dates - individual exceptions where no entry should be created</li>
 *   <li>Active status - allows temporarily disabling without deletion</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "recurring_reservation")
public class RecurringReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the recurring reservation", example = "1")
    private Long id;

    @Column(name = "employee_name", nullable = false)
    @Schema(description = "Name of the employee", example = "Max Mustermann")
    private String employeeName;

    @Column(name = "start_date", nullable = false)
    @Schema(description = "Start date of the recurring reservation period", example = "2025-01-15")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @Schema(description = "End date of the recurring reservation period", example = "2025-12-31")
    private LocalDate endDate;

    /**
     * Set of dates to skip when generating parking entries.
     * <p>
     * Even if a date falls on a configured day of the week and is within the
     * date range, no parking entry will be created if the date is in this set.
     * This allows for individual exceptions (e.g., holidays, vacation days).
     * </p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "recurring_reservation_skip_date",
            joinColumns = @JoinColumn(name = "recurring_reservation_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_reservation_skip_date",
                    columnNames = {"recurring_reservation_id", "skip_date"}
            )
    )
    @Column(name = "skip_date", nullable = false)
    @Schema(description = "Dates to skip when generating parking entries")
    private Set<LocalDate> skipDates = new HashSet<>();

    /**
     * Collection of parking entries automatically generated from this recurring reservation.
     * <p>
     * This is the owning side of the bidirectional relationship. All parking entries
     * are automatically deleted when the recurring reservation is deleted (cascade delete)
     * or when entries are removed from this collection.
     * </p>
     */
    @OneToMany(mappedBy = "recurringReservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "Collection of parking entries generated from this recurring reservation", hidden = true)
    private Set<ParkingEntry> parkingEntries = new HashSet<>();

    /**
     * Days of the week on which parking spots should be reserved.
     * <p>
     * Parking entries are generated only for dates that match one of these
     * days of the week, are within the start/end date range, and are not
     * in the skip dates set.
     * </p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "recurring_reservation_day",
            joinColumns = @JoinColumn(name = "recurring_reservation_id")
    )
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Days of the week on which parking spots should be reserved", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
    private Set<DayOfWeek> daysOfWeek;

    /**
     * Flag indicating whether this recurring reservation is active.
     * <p>
     * When set to false, the recurring reservation is disabled and no new
     * parking entries will be generated. Existing entries are not affected.
     * This allows temporarily suspending a recurring reservation without
     * losing the configuration.
     * </p>
     */
    @Column(name = "active", nullable = false)
    @Schema(description = "Flag indicating whether this recurring reservation is active", example = "true")
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Set<LocalDate> getSkipDates() {
        return skipDates;
    }

    public void setSkipDates(Set<LocalDate> skipDates) {
        this.skipDates = skipDates;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Set<ParkingEntry> getParkingEntries() {
        return parkingEntries;
    }

    public void setParkingEntries(Set<ParkingEntry> parkingEntries) {
        this.parkingEntries = parkingEntries;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}


