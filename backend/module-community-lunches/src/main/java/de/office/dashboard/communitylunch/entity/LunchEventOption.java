package de.office.dashboard.communitylunch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

/**
 * Entity representing a meal option for a community lunch event.
 * <p>
 * Each option represents a meal choice that employees can vote for. Options can be either
 * from the food catalog (reusable) or custom (event-specific). The two are mutually exclusive -
 * an option must have either a catalog item or a custom label, but not both.
 * </p>
 * <p>
 */
@Schema(description = "Meal option for a community lunch event")
@Entity
@Table(
        name = "lunch_event_option",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_leo_event_catalog",
                        columnNames = {"event_id", "catalog_item_id"}
                )
        }
)
public class LunchEventOption {

    @Schema(description = "Unique identifier", example = "5")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Parent lunch event")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CommunityLunchEvent event;

    @Schema(description = "Reference to catalog item (if from catalog)", example = "3")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id")
    private FoodCatalogItem catalogItem;

    @Schema(description = "Custom label (if not from catalog)", example = "Vegetarian Pizza", maxLength = 255)
    @Column(name = "custom_label", length = 255)
    private String customLabel;

    @Schema(description = "Whether option is available for voting", example = "true")
    @Column(nullable = false)
    private boolean active = true;

    @Schema(description = "Custom sort order (optional)", example = "1")
    @Column(name = "sort_order")
    private Integer sortOrder;

    public LunchEventOption() {
    }

    /**
     * Validates that the option has exactly one of catalogItem or customLabel set.
     *
     * @throws IllegalArgumentException if both are set or both are null/blank
     */
    public void validate() {
        boolean hasCatalog = (catalogItem != null);
        boolean hasCustom = (customLabel != null && !customLabel.isBlank());
        if (hasCatalog == hasCustom) {
            throw new IllegalArgumentException("Option must have EITHER catalogItem OR customLabel");
        }
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

    public FoodCatalogItem getCatalogItem() {
        return catalogItem;
    }

    public void setCatalogItem(FoodCatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
