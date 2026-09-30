package de.office.dashboard.communitylunch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a reusable food catalog item.
 * <p>
 * Catalog items are meal options that can be reused across multiple lunch events.
 * </p>
 * <p>
 * Constraints:
 * </p>
 * <ul>
 *   <li>Unique label</li>
 *   <li>Soft delete: active flag instead of deletion</li>
 *   <li>Reactivation: creating item with existing label reactivates it</li>
 * </ul>
 */
@Schema(description = "Reusable food catalog item for meal options")
@Entity
@Table(
        name = "food_catalog_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_food_catalog_item_label",
                columnNames = "label"
        )
)
public class FoodCatalogItem {

    @Schema(description = "Unique identifier", example = "3")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Food item label (unique)", example = "Margherita Pizza", maxLength = 255)
    @Column(nullable = false, length = 255)
    private String label;

    @Schema(description = "Whether item is active (soft delete flag)", example = "true")
    @Column(nullable = false)
    private boolean active = true;

    @Schema(description = "Creation timestamp (auto-set)", example = "2026-01-20T09:00:00")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp (auto-updated)", example = "2026-01-25T11:30:00")
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public FoodCatalogItem() {
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

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
