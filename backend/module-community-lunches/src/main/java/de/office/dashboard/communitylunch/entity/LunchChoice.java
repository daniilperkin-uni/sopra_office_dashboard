package de.office.dashboard.communitylunch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing an employee's meal choice for a specific lunch event.
 * <p>
 * Tracks which meal option an employee has voted for in a given event.
 * Implements upsert behavior - if employee votes again for same event, choice is updated.
 * Only one choice per employee per event enforced by unique constraint.
 * </p>
 * <p>
 */
@Schema(description = "Employee meal choice for a lunch event")
@Entity
@Table(
        name = "lunch_choice",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lc_event_employee",
                columnNames = {"event_id", "employee_name"}
        )
)
public class LunchChoice {

    @Schema(description = "Unique identifier", example = "10")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "The lunch event")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CommunityLunchEvent event;

    @Schema(description = "The chosen meal option")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private LunchEventOption option;

    @Schema(description = "Employee name", example = "Max Mustermann", maxLength = 255)
    @Column(name = "employee_name", nullable = false, length = 255)
    private String employeeName;

    @Schema(description = "Creation timestamp (auto-set)", example = "2026-01-25T12:15:00")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LunchChoice() {
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public CommunityLunchEvent getEvent() {
        return event;
    }

    public void setEvent(CommunityLunchEvent event) {
        this.event = event;
    }

    public LunchEventOption getOption() {
        return option;
    }

    public void setOption(LunchEventOption option) {
        this.option = option;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
