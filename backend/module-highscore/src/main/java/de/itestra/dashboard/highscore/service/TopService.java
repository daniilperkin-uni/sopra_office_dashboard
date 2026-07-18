package de.itestra.dashboard.highscore.service;

import de.itestra.dashboard.highscore.MatchResult;
import de.itestra.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.itestra.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.itestra.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.itestra.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import de.itestra.dashboard.highscore.mapper.DartsEntryMapper;
import de.itestra.dashboard.highscore.mapper.KickerEntryMapper;
import de.itestra.dashboard.highscore.projection.DartsPlayerProjection;
import de.itestra.dashboard.highscore.projection.PlayerPointsProjection;
import de.itestra.dashboard.highscore.repository.DartsEntryRepository;
import de.itestra.dashboard.highscore.repository.KickerEntryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Service managing leaderboards and CRUD operations for Darts and Kicker game entries.
 * <p>
 * This service handles:
 * <ul>
 *   <li>Leaderboard calculations (top 3 players/teams)</li>
 *   <li>Match creation, updating, and deletion for both games</li>
 *   <li>Team validation for kicker matches</li>
 *   <li>Caching of leaderboard data for performance optimization</li>
 * </ul>
 * </p>
 * <p>
 * Rules:
 * <ul>
 *   <li>Darts: Ranked by fewest darts to finish (lower is better)</li>
 *   <li>Kicker: Ranked by total match wins for each team combination</li>
 *   <li>Kicker teams must have 1-2 players per team</li>
 *   <li>No player can appear in both teams in the same match</li>
 * </ul>
 * </p>
 */
@Service
public class TopService {

    private final DartsEntryRepository dartsEntryRepository;
    private final KickerEntryRepository kickerEntryRepository;
    private final DartsEntryMapper dartsEntryMapper;
    private final KickerEntryMapper kickerEntryMapper;

    /**
     * Constructs a new TopService.
     *
     * @param dartsEntryRepository repository for darts match data access
     * @param kickerEntryRepository repository for kicker match data access
     * @param dartsEntryMapper mapper for darts entry responses
     * @param kickerEntryMapper mapper for kicker entry responses
     */
    public TopService(DartsEntryRepository dartsEntryRepository,
                      KickerEntryRepository kickerEntryRepository, DartsEntryMapper dartsEntryMapper,
                      KickerEntryMapper kickerEntryMapper) {
        this.dartsEntryRepository = dartsEntryRepository;
        this.kickerEntryRepository = kickerEntryRepository;
        this.dartsEntryMapper = dartsEntryMapper;
        this.kickerEntryMapper = kickerEntryMapper;
    }

    /**
     * Retrieves the top 3 darts players ranked by fewest darts to finish.
     * <p>
     * Results are cached to improve performance. Cache is invalidated when
     * darts entries are created, updated, or deleted.
     * </p>
     *
     * @return list of top 3 darts players with their best performances
     */
    @Cacheable("dartsTop3")
    public List<DartsPlayerProjection> getDartsTop3() {
        return dartsEntryRepository.findTop(PageRequest.of(0, 3));
    }

    /**
     * Retrieves the top 3 kicker teams ranked by total match wins.
     * <p>
     * Teams are identified by player combinations (order-independent).
     * Results are cached for performance.
     * </p>
     *
     * @return list of top 3 kicker teams with their total points
     */
    @Cacheable("kickerTop3")
    public List<PlayerPointsProjection> getKickerTop3() {
        return kickerEntryRepository.findKickerTop3Teams();
    }

    /**
     * Retrieves all darts matches, ordered by creation date (newest first).
     *
     * @return list of all darts match entries
     */
    public List<DartsEntry> getAllDartsMatches() {
        return dartsEntryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * Retrieves all kicker matches, ordered by creation date (newest first).
     *
     * @return list of all kicker match entries
     */
    public List<KickerEntry> getAllKickerMatches() {
        return kickerEntryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * Creates a new darts match entry.
     *
     * @param request the darts entry details (player name and darts to finish)
     * @return the created darts entry as a response DTO
     */
    @Transactional
    public DartsEntryResponse createDartsEntry(DartsEntryRequest request) {
        DartsEntry e = new DartsEntry();
        e.setPlayerName(request.playerName());
        e.setCreatedAt(LocalDateTime.now());
        e.setDartsToFinish(request.dartsToFinish());
        dartsEntryRepository.save(e);
        return dartsEntryMapper.toResponse(e);
    }

    /**
     * Updates an existing darts match entry.
     *
     * @param id the unique identifier of the darts entry to update
     * @param request the updated darts entry details
     * @return the updated darts entry as a response DTO
     * @throws NoSuchElementException if no darts entry exists with the given ID
     */
    @Transactional
    public DartsEntryResponse updateDartsEntry(Long id, DartsEntryRequest request) {
        DartsEntry entry = dartsEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Darts entry not found: " + id));
        entry.setPlayerName(request.playerName());
        entry.setDartsToFinish(request.dartsToFinish());

        return dartsEntryMapper.toResponse(entry);
    }

    /**
     * Deletes a darts match entry by its ID.
     *
     * @param id the unique identifier of the darts entry to delete
     */
    @Transactional
    public void deleteDartsEntry(Long id) {
        dartsEntryRepository.deleteById(id);
    }

    /**
     * Creates a new kicker match entry after validating team composition.
     *
     * @param request the kicker entry details (teams and match result)
     * @return the created kicker entry as a response DTO
     * @throws IllegalArgumentException if team validation fails
     */
    @Transactional
    public KickerEntryResponse createKickerEntry(KickerEntryRequest request) {
        validateTeams(request);
        List<String> teamA = request.teamAPlayers();
        List<String> teamB = request.teamBPlayers();
        MatchResult matchResult = request.matchResult();
        LocalDateTime now = LocalDateTime.now();

        KickerEntry match = new KickerEntry();
        match.setTeamAPlayerNames(teamA);
        match.setTeamBPlayerNames(teamB);
        match.setCreatedAt(now);
        match.setMatchResult(matchResult);
        kickerEntryRepository.save(match);
        return kickerEntryMapper.toResponse(match);
    }

    /**
     * Updates an existing kicker match entry after validating team composition.
     *
     * @param id the unique identifier of the kicker entry to update
     * @param request the updated kicker entry details
     * @return the updated kicker entry as a response DTO
     * @throws NoSuchElementException if no kicker entry exists with the given ID
     * @throws IllegalArgumentException if team validation fails
     */
    @Transactional
    public KickerEntryResponse updateKickerEntry(Long id, KickerEntryRequest request) {
        KickerEntry entry = kickerEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kicker entry not found: " + id));
        validateTeams(request);

        entry.setTeamAPlayerNames(request.teamAPlayers());
        entry.setTeamBPlayerNames(request.teamBPlayers());
        entry.setMatchResult(request.matchResult());

        return kickerEntryMapper.toResponse(entry);
    }

    /**
     * Deletes a kicker match entry by its ID.
     *
     * @param id the unique identifier of the kicker entry to delete
     */
    @Transactional
    public void deleteKickerEntry(Long id) {
        kickerEntryRepository.deleteById(id);
    }

    /**
     * Validates kicker team composition according to game rules.
     * <p>
     * Validation Rules:
     * <ul>
     *   <li>Each team must have 1 or 2 players</li>
     *   <li>No player can appear in both teams</li>
     *   <li>Each player can only appear once across all teams</li>
     * </ul>
     * </p>
     *
     * @param request the kicker entry request to validate
     * @throws IllegalArgumentException if teams have invalid size or contain duplicate players
     */
    private void validateTeams(KickerEntryRequest request) {
        int teamA = request.teamAPlayers().size();
        int teamB = request.teamBPlayers().size();
        if (teamA < 1 || teamA > 2 || teamB < 1 || teamB > 2) {
            throw new IllegalArgumentException("Teams must be 1 or 2 players");
        }
        Set<String> all = new HashSet<>();
        all.addAll(request.teamAPlayers());
        all.addAll(request.teamBPlayers());
        if (all.size() != teamA + teamB) {
            throw new IllegalArgumentException("Duplicate player in match");
        }
    }
}
