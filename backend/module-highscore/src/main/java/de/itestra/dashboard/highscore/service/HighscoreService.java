package de.itestra.dashboard.highscore.service;

import de.itestra.dashboard.highscore.dto.OverviewResponse;
import de.itestra.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.itestra.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.itestra.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.itestra.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Main orchestration service for highscore management.
 * <p>
 * This service coordinates between {@link TopService} and {@link MatchHistoryService}
 * and implements a comprehensive caching strategy to optimize performance.
 * </p>
 * <p>
 * Caching Strategy:
 * <ul>
 *   <li>overview cache: Stores combined leaderboards and match history</li>
 *   <li>dartsTop3cache: Stores top 3 darts players</li>
 *   <li>kickerTop3 cache: Stores top 3 kicker teams</li>
 *   <li>matchHistory cache: Stores recent match history</li>
 * </ul>
 * Caches are automatically invalidated when matches are created, updated, or deleted.
 * </p>
 */
@Service
public class HighscoreService {

    private final TopService topService;
    private final MatchHistoryService matchHistoryService;

    /**
     * Constructs a new HighscoreService.
     *
     * @param topService service managing leaderboards and match operations
     * @param matchHistoryService service providing match history data
     */
    public HighscoreService(TopService topService, MatchHistoryService matchHistoryService) {
        this.topService = topService;
        this.matchHistoryService = matchHistoryService;
    }

    /**
     * Retrieves the complete highscore overview including leaderboards and match history.
     * <p>
     * Combines top 3 darts players, top 3 kicker teams, and recent match history into
     * a single response. Results are cached for performance.
     * </p>
     *
     * @return overview response containing leaderboards and match history
     */
    @Cacheable("overview")
    public OverviewResponse getOverview() {
        return new OverviewResponse(
                topService.getDartsTop3(),
                topService.getKickerTop3(),
                matchHistoryService.getMatchHistory()
        );
    }

    /**
     * Retrieves all darts matches ordered by creation date (newest first).
     *
     * @return list of all darts match entries
     */
    public List<DartsEntry> getAllDartsMatches() {
        return topService.getAllDartsMatches();
    }

    /**
     * Retrieves all kicker matches ordered by creation date (newest first).
     *
     * @return list of all kicker match entries
     */
    public List<KickerEntry> getAllKickerMatches() {
        return topService.getAllKickerMatches();
    }

    /**
     * Creates a new darts match entry.
     * <p>
     * Automatically evicts caches for darts leaderboard, match history, and overview
     * to ensure fresh data is retrieved on next access.
     * </p>
     *
     * @param request the darts entry details
     * @return the created darts entry as a response DTO
     */
    @CacheEvict(
            value = {"dartsTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public DartsEntryResponse createDartsEntry(DartsEntryRequest request) {
        return topService.createDartsEntry(request);
    }

    /**
     * Updates an existing darts match entry.
     * <p>
     * Automatically invalidates caches for darts leaderboard, match history, and overview.
     * </p>
     *
     * @param id the unique identifier of the darts entry to update
     * @param request the updated darts entry details
     * @return the updated darts entry as a response DTO
     */
    @CacheEvict(
            value = {"dartsTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public DartsEntryResponse updateDartsEntry(Long id, DartsEntryRequest request) {
        return topService.updateDartsEntry(id, request);
    }

    /**
     * Deletes a darts match entry.
     * <p>
     * Automatically invalidates caches for darts leaderboard, match history, and overview.
     * </p>
     *
     * @param id the unique identifier of the darts entry to delete
     */
    @CacheEvict(
            value = {"dartsTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public void deleteDartsEntry(Long id) {
        topService.deleteDartsEntry(id);
    }

    /**
     * Creates a new kicker match entry.
     * <p>
     * Automatically invalidates caches for kicker leaderboard, match history, and overview
     * to ensure fresh data is retrieved on next access.
     * </p>
     *
     * @param request the kicker entry details
     * @return the created kicker entry as a response DTO
     */
    @CacheEvict(
            value = {"kickerTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public KickerEntryResponse createKickerEntry(KickerEntryRequest request) {
        return topService.createKickerEntry(request);
    }

    /**
     * Updates an existing kicker match entry.
     * <p>
     * Automatically invalidates caches for kicker leaderboard, match history, and overview.
     * </p>
     *
     * @param id the unique identifier of the kicker entry to update
     * @param request the updated kicker entry details
     * @return the updated kicker entry as a response DTO
     */
    @CacheEvict(
            value = {"kickerTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public KickerEntryResponse updateKickerEntry(Long id, KickerEntryRequest request) {
        return topService.updateKickerEntry(id, request);
    }

    /**
     * Deletes a kicker match entry.
     * <p>
     * Automatically invalidates caches for kicker leaderboard, match history, and overview.
     * </p>
     *
     * @param id the unique identifier of the kicker entry to delete
     */
    @CacheEvict(
            value = {"kickerTop3", "matchHistory", "overview"},
            allEntries = true
    )
    public void deleteKickerEntry(Long id) {
        topService.deleteKickerEntry(id);
    }
}
