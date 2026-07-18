package de.itestra.dashboard.communitylunch.mapper;

import de.itestra.dashboard.communitylunch.dto.option.response.LunchEventOptionResponse;
import de.itestra.dashboard.communitylunch.entity.LunchEventOption;

/**
 * Mapper for converting {@link LunchEventOption} entities to {@link LunchEventOptionResponse} DTOs.
 * <p>
 * Handles label resolution from catalog or custom sources.
 * </p>
 */
public final class LunchOptionResponseMapper {
    private LunchOptionResponseMapper() {
    }

    /**
     * Converts a lunch event option entity to a response DTO.
     * <p>
     * Uses {@link LunchOptionMapper#label(LunchEventOption)} to resolve the label
     * from either catalog item or custom label.
     * </p>
     *
     * @param option the lunch event option entity to convert
     * @return the lunch event option response DTO
     */
    public static LunchEventOptionResponse toResponse(LunchEventOption option) {
        return new LunchEventOptionResponse(
                option.getId(),
                LunchOptionMapper.label(option),
                option.isActive()
        );
    }
}
