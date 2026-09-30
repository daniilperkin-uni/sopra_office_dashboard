package de.office.dashboard.communitylunch.repository;

import de.office.dashboard.communitylunch.entity.FoodCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for {@link FoodCatalogItem} entities.
 * <p>
 * Provides data access methods for food catalog items. Supports case-insensitive
 * label matching for duplicate prevention.
 * </p>
 */
public interface FoodCatalogItemRepository extends JpaRepository<FoodCatalogItem, Long> {
    /**
     * Finds a catalog item by label, ignoring case.
     * <p>
     * Used for duplicate checking and reactivation logic when creating new items.
     * </p>
     *
     * @param label the label to search for (case-insensitive)
     * @return optional containing the item if found, or empty otherwise
     */
    Optional<FoodCatalogItem> findByLabelIgnoreCase(String label);

    /**
     * Checks if another catalog item exists with the same label (case-insensitive).
     * <p>
     * Used when updating an item to prevent duplicate labels. Excludes the item
     * being updated from the check.
     * </p>
     *
     * @param label the label to check (case-insensitive)
     * @param id the ID of the item being updated (to exclude from check)
     * @return true if another item with this label exists, false otherwise
     */
    boolean existsByLabelIgnoreCaseAndIdNot(String label, Long id);
}

