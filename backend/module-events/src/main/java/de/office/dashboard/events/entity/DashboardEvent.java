package de.office.dashboard.events.entity;

import de.office.dashboard.events.DashboardEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * JPA entity representing a dashboard event.
 * <p>
 * A dashboard event represents an employee-related event such as a birthday,
 * work anniversary, or probation ending. Each event is associated with an employee
 * and has both an original event date and a dashboard display date.
 * </p>
 */
@Entity
@Table(name = "dashboard_event")
public class DashboardEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "2")
    private Long id;

    @Column(name = "employee_name", nullable = false)
    @Schema(description = "Name of the employee", example = "Max Mustermann")
    private String employeeName;

    @Column(name = "employee_email", nullable = false)
    @Schema(description = "Email of the employee", example = "mustermann@example.com")
    private String employeeEmail;

    @Schema(description = "Unique identifier of the employee", example = "2")
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    private DashboardEventType dashboardEventType;

    @Column(name = "original_event_date")
    @Schema(name = "Original date of the event ", example = "24.12.2000")
    private LocalDate originalEventDate;

    @Column(name = "dashboard_event_date")
    @Schema(name = "Date of the dashboard event", example = "24.12.2025")
    private LocalDate dashboardEventDate;

    /**
     * Default constructor for JPA.
     */
    public DashboardEvent() {
    }

    /**
     * Constructs a new DashboardEvent with all required fields.
     *
     * @param employeeId         the unique identifier of the employee
     * @param employeeName       the name of the employee
     * @param employeeEmail      the email address of the employee (only used if dashboardEventType = BIRTHDAY)
     * @param dashboardEventType the type of dashboard event
     * @param dashboardEventDate the date when the event occurs on the dashboard
     * @param originalEventDate  the original date of the event (e.g., actual birthday)
     */
    public DashboardEvent(
            Long employeeId,
            String employeeName,
            String employeeEmail,
            DashboardEventType dashboardEventType,
            LocalDate dashboardEventDate,
            LocalDate originalEventDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.dashboardEventType = dashboardEventType;
        this.originalEventDate = originalEventDate;
        this.dashboardEventDate = dashboardEventDate;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public DashboardEventType getDashboardEventType() {
        return dashboardEventType;
    }

    public void setDashboardEventType(DashboardEventType dashboardEventType) {
        this.dashboardEventType = dashboardEventType;
    }

    public LocalDate getOriginalEventDate() {
        return originalEventDate;
    }

    public void setOriginalEventDate(LocalDate originalEventDate) {
        this.originalEventDate = originalEventDate;
    }

    public LocalDate getDashboardEventDate() {
        return dashboardEventDate;
    }

    public void setDashboardEventDate(LocalDate dashboardEventDate) {
        this.dashboardEventDate = dashboardEventDate;
    }
}
