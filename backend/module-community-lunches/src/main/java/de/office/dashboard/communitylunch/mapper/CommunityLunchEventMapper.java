package de.office.dashboard.communitylunch.mapper;

import de.office.dashboard.communitylunch.dto.event.response.CommunityLunchEventResponse;
import de.office.dashboard.communitylunch.dto.option.response.LunchEventOptionResponse;
import de.office.dashboard.communitylunch.entity.CommunityLunchEvent;
import de.office.dashboard.communitylunch.entity.LunchEventOption;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper for converting {@link CommunityLunchEvent} entities to {@link CommunityLunchEventResponse} DTOs.
 */
public final class CommunityLunchEventMapper {
    private CommunityLunchEventMapper() {
    }

    /**
     * Converts a community lunch event entity to a response DTO.
     * <p>
     * Options are sorted first by sortOrder (nulls last), then alphabetically
     * by label (case-insensitive).
     * </p>
     *
     * @param event the lunch event entity to convert
     * @return the lunch event response DTO
     */
    public static CommunityLunchEventResponse toResponse(CommunityLunchEvent event) {
        List<LunchEventOptionResponse> options = event.getOptions().stream()
                .sorted(Comparator
                        .comparing((LunchEventOption o) -> o.getSortOrder() == null ? Integer.MAX_VALUE : o.getSortOrder())
                        .thenComparing(LunchOptionMapper::label, String.CASE_INSENSITIVE_ORDER)
                )
                .map(LunchOptionResponseMapper::toResponse)
                .toList();

        return new CommunityLunchEventResponse(
                event.getId(),
                event.getDate(),
                event.getLocation(),
                event.getStatus().name(),
                event.getNote(),
                event.getUpdatedAt(),
                options
        );
    }
}
