package de.office.dashboard.communitylunch.service;

import de.office.dashboard.communitylunch.dto.catalog.request.CreateFoodCatalogItemRequest;
import de.office.dashboard.communitylunch.dto.catalog.request.UpdateFoodCatalogItemRequest;
import de.office.dashboard.communitylunch.dto.catalog.response.FoodCatalogItemResponse;
import de.office.dashboard.communitylunch.entity.FoodCatalogItem;
import de.office.dashboard.communitylunch.mapper.FoodCatalogItemMapper;
import de.office.dashboard.communitylunch.repository.FoodCatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service for managing the food catalog of reusable meal options.
 */
@Service
public class FoodCatalogService {

    private final FoodCatalogItemRepository foodCatalogItemRepository;

    /**
     * Constructs a new FoodCatalogService.
     *
     * @param foodCatalogItemRepository repository for food catalog items
     */
    public FoodCatalogService(FoodCatalogItemRepository foodCatalogItemRepository) {
        this.foodCatalogItemRepository = foodCatalogItemRepository;
    }

    /**
     * Retrieves all food catalog items, optionally filtered to show only active items.
     * <p>
     * When activeOnly is false, includes both active and inactive items.
     * </p>
     *
     * @param activeOnly if true, returns only active items; if false, returns all items
     * @return list of catalog items
     */
    @Transactional(readOnly = true)
    public List<FoodCatalogItemResponse> getCatalogItems(boolean activeOnly) {
        List<FoodCatalogItem> items = foodCatalogItemRepository.findAll();
        if (activeOnly) {
            items = items.stream().filter(FoodCatalogItem::isActive).toList();
        }
        return items.stream().map(FoodCatalogItemMapper::toResponse).toList();
    }

    /**
     * Creates a new food catalog item or reactivates an existing inactive item.
     * <p>
     * Uses case-insensitive label matching. If an inactive item with the same label exists,
     * it will be reactivated instead of creating a duplicate. 
     * </p>
     *
     * @param request request containing the catalog item label
     * @return the created or reactivated catalog item
     * @throws IllegalArgumentException if an active item with this label already exists
     */
    @Transactional
    public FoodCatalogItemResponse createCatalogItem(CreateFoodCatalogItemRequest request) {
        String label = request.label().trim();

        FoodCatalogItem item = foodCatalogItemRepository.findByLabelIgnoreCase(label)
                .map(existing -> {
                    if (existing.isActive()) {
                        throw new IllegalArgumentException("This item is already in the catalog.");
                    }
                    existing.setActive(true);
                    return existing;
                })
                .orElseGet(() -> {
                    FoodCatalogItem created = new FoodCatalogItem();
                    created.setLabel(label);
                    created.setActive(true);
                    return foodCatalogItemRepository.save(created);
                });

        return FoodCatalogItemMapper.toResponse(item);
    }

    /**
     * Updates the label of an existing catalog item.
     *
     * @param id unique identifier of the catalog item
     * @param request request containing the new label
     * @return the updated catalog item
     * @throws NoSuchElementException if catalog item not found
     * @throws IllegalArgumentException if another active item with this label already exists
     */
    @Transactional
    public FoodCatalogItemResponse updateCatalogItem(Long id, UpdateFoodCatalogItemRequest request) {
        String newLabel = request.label().trim();

        FoodCatalogItem item = foodCatalogItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Catalog item not found: " + id));

        if (item.getLabel().equalsIgnoreCase(newLabel)) {
            item.setLabel(newLabel);
            return FoodCatalogItemMapper.toResponse(item);
        }

        if (foodCatalogItemRepository.existsByLabelIgnoreCaseAndIdNot(newLabel, id)) {
            throw new IllegalArgumentException("This item is already in the catalog.");
        }

        item.setLabel(newLabel);
        return FoodCatalogItemMapper.toResponse(item);
    }

    /**
     * Activates or deactivates a catalog item.
     * <p>
     * Setting active to false implements. Inactive items can be
     * reactivated by creating a new item with the same label.
     * </p>
     *
     * @param id unique identifier of the catalog item
     * @param active whether the item should be active
     * @throws NoSuchElementException if catalog item not found
     */
    @Transactional
    public void updateCatalogItemActive(Long id, boolean active) {
        FoodCatalogItem item = foodCatalogItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Catalog item not found: " + id));

        item.setActive(active);
    }
}

