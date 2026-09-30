package de.office.dashboard.parking.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity representing a parking space reservation for a specific date.
 * <p>
 * A parking entry reserves one parking spot for an employee on a given day.
 * Entries can be created manually by employees or automatically generated
 * from recurring reservations. Each employee can have only one reservation
 * per day through a unique constraint.
 * </p>
 */
@Entity
@Table(name = "parking_entry",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_date", columnNames = {"employee_name", "entry_date"}
        ))

public class ParkingEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "2")
    private Long id;

    @Column(name = "employee_name", nullable = false)
    @Schema(description = "Name of the employee", example = "Max Mustermann")
    private String employeeName;

    @Column(name = "entry_date", nullable = false)
    @Schema(name = "Reservation date", example = "24-02-2026")
    private LocalDate date;

    /**
     * Optional reference to the recurring reservation that generated this entry.
     * <p>
     * If this field is null, the parking entry was created manually.
     * If it references a RecurringReservation, the entry was automatically
     * generated based on recurring rules.
     * </p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recurring_reservation_id")
    @Schema(description = "Reference to the recurring reservation that generated this entry (null if created manually)")
    private RecurringReservation recurringReservation;

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public RecurringReservation getRecurringReservation() {
        return recurringReservation;
    }

    public void setRecurringReservation(RecurringReservation recurringReservation) {
        this.recurringReservation = recurringReservation;
    }
}
