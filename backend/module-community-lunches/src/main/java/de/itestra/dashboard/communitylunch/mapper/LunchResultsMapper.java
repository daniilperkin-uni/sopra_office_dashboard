package de.itestra.dashboard.communitylunch.mapper;

import de.itestra.dashboard.communitylunch.dto.choice.response.LunchResultsResponse;
import de.itestra.dashboard.communitylunch.entity.CommunityLunchEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting voting data into {@link LunchResultsResponse} DTOs.
 */
public final class LunchResultsMapper {
    private LunchResultsMapper() {
    }

    /**
     * Converts an event and vote counts map into a voting results response DTO.
     * <p>
     * Results are sorted by vote count in descending order (most popular first).
     * Options with no votes will have a count of 0.
     * </p>
     *
     * @param event the lunch event
     * @param countsByOptionId map of option IDs to vote counts
     * @return the voting results response DTO with sorted results
     */
    public static LunchResultsResponse toResponse(CommunityLunchEvent event, Map<Long, Long> countsByOptionId) {
        List<LunchResultsResponse.OptionResult> results = event.getOptions().stream()
                .map(opt -> new LunchResultsResponse.OptionResult(
                        opt.getId(),
                        LunchOptionMapper.label(opt),
                        countsByOptionId.getOrDefault(opt.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(LunchResultsResponse.OptionResult::count).reversed())
                .toList();

        return new LunchResultsResponse(event.getId(), results);
    }
}
