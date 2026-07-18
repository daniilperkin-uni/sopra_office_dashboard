package de.itestra.dashboard.communitylunch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a community lunch event.
 * <p>
 * Each event represents a community lunch on a specific date with a location,
 * optional notes, and a status controlling the voting lifecycle. Events have
 * a one-to-many relationship with meal options that employees can vote on.
 * </p>
 * <p>
 * Constraints:
 * </p>
 * <ul>
 *   <li>Unique date: only one event per day</li>
 *   <li>Status: DRAFT → OPEN → CLOSED</li>
 *   <li>Cascade delete: removing event deletes all options and choices</li>
 * </ul>
 */
@Schema(description = "Community lunch event with meal options and voting")
@Entity
@Table(
        name = "community_lunch_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cle_date",
                columnNames = "event_date"
        )
)
public class CommunityLunchEvent {

    @Schema(description = "Unique identifier", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Event date (unique, one event per day)", example = "2026-02-14")
    @FutureOrPresent(message = "Das Eventdatum darf nicht in der Vergangenheit liegen")
    @Column(name = "event_date", nullable = false)
    private LocalDate date;

    @Schema(description = "Event location", example = "Cafeteria", maxLength = 255)
    @Column(nullable = false, length = 255)
    private String location;

    @Schema(description = "Optional event notes", example = "Valentine's Day special menu", maxLength = 1000)
    @Column(length = 1000)
    private String note;

    @Schema(description = "Event status controlling voting (DRAFT, OPEN, or CLOSED)", example = "OPEN")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LunchStatus status = LunchStatus.DRAFT;

    @Schema(description = "Creation timestamp (auto-set)", example = "2026-01-25T10:30:00")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp (auto-updated)", example = "2026-01-26T14:20:00")
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Schema(description = "Meal options for this event")
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LunchEventOption> options = new ArrayList<>();

    public CommunityLunchEvent() {
    }

    @PrePersist
    void prePersist() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add an option to this event.
     *
     * @param option the option to add
     */
    public void addOption(LunchEventOption option) {
        options.add(option);
        option.setEvent(this);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LunchStatus getStatus() {
        return status;
    }

    public void setStatus(LunchStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LunchEventOption> getOptions() {
        return options;
    }

    public void setOptions(List<LunchEventOption> options) {
        this.options = options;
    }
}
