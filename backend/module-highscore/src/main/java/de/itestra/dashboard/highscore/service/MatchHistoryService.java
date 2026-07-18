package de.itestra.dashboard.highscore.service;

import de.itestra.dashboard.highscore.dto.matchhistory.response.MatchHistoryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import de.itestra.dashboard.highscore.mapper.MatchHistoryEntryMapper;
import de.itestra.dashboard.highscore.repository.DartsEntryRepository;
import de.itestra.dashboard.highscore.repository.KickerEntryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for retrieving and aggregating match history from both Darts and
 * Kicker games.
 * <p>
 * This service combines recent matches from both games into a unified
 * chronological
 * history view, showing the most recent activity across all game types.
 * </p>
 */
@Service
public class MatchHistoryService {

    private final DartsEntryRepository dartsEntryRepository;
    private final KickerEntryRepository kickerEntryRepository;
    private final MatchHistoryEntryMapper matchHistoryEntryMapper;

    /**
     * Constructs a new MatchHistoryService.
     *
     * @param dartsEntryRepository    repository for darts match data access
     * @param kickerEntryRepository   repository for kicker match data access
     * @param matchHistoryEntryMapper mapper for converting entries to unified
     *                                history responses
     */
    public MatchHistoryService(DartsEntryRepository dartsEntryRepository,
            KickerEntryRepository kickerEntryRepository,
            MatchHistoryEntryMapper matchHistoryEntryMapper) {
        this.dartsEntryRepository = dartsEntryRepository;
        this.kickerEntryRepository = kickerEntryRepository;
        this.matchHistoryEntryMapper = matchHistoryEntryMapper;
    }

    /**
     * Retrieves the combined match history from both Darts and Kicker games.
     * <p>
     * Fetches the 5 most recent matches from each game, combines them into a
     * unified
     * format, and sorts by date (newest first). Results are cached for performance.
     * </p>
     *
     * @return list of recent matches from both games, sorted by date (newest first)
     */
    @Cacheable("matchHistory")
    public List<MatchHistoryResponse> getMatchHistory() {
        List<DartsEntry> darts = dartsEntryRepository.findLastMatches(PageRequest.of(0, 5));
        List<KickerEntry> kicker = kickerEntryRepository.findLastEntries(PageRequest.of(0, 5));

        return Stream.concat(darts.stream().map(matchHistoryEntryMapper::toResponse),
                kicker.stream().map(matchHistoryEntryMapper::toResponse))
                .sorted(Comparator.comparing(MatchHistoryResponse::date).reversed())
                .toList();
    }
}
