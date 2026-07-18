package de.itestra.dashboard.communitylunch.mapper;

import de.itestra.dashboard.communitylunch.dto.catalog.response.FoodCatalogItemResponse;
import de.itestra.dashboard.communitylunch.entity.FoodCatalogItem;

/**
 * Mapper for converting {@link FoodCatalogItem} entities to {@link FoodCatalogItemResponse} DTOs.
 */
public final class FoodCatalogItemMapper {
    private FoodCatalogItemMapper() {
    }

    /**
     * Converts a food catalog item entity to a response DTO.
     *
     * @param item the food catalog item entity to convert
     * @return the food catalog item response DTO
     */
    public static FoodCatalogItemResponse toResponse(FoodCatalogItem item) {
        return new FoodCatalogItemResponse(
                item.getId(),
                item.getLabel(),
                item.isActive()
        );
    }
}
