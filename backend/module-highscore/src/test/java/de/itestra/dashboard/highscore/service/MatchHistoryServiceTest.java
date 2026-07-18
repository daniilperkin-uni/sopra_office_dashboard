// src/test/java/de/itestra/dashboard/highscore/service/MatchHistoryServiceTest.java
package de.itestra.dashboard.highscore.service;

import de.itestra.dashboard.highscore.MatchResult;
import de.itestra.dashboard.highscore.dto.matchhistory.response.MatchHistoryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import de.itestra.dashboard.highscore.mapper.MatchHistoryEntryMapper;
import de.itestra.dashboard.highscore.repository.DartsEntryRepository;
import de.itestra.dashboard.highscore.repository.KickerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchHistoryServiceTest {

    DartsEntryRepository dartsEntryRepository;
    KickerEntryRepository kickerEntryRepository;
    MatchHistoryEntryMapper matchHistoryEntryMapper;

    MatchHistoryService matchHistoryService;

    @BeforeEach
    void setUp() {
        dartsEntryRepository = Mockito.mock(DartsEntryRepository.class);
        kickerEntryRepository = Mockito.mock(KickerEntryRepository.class);
        matchHistoryEntryMapper = Mockito.mock(MatchHistoryEntryMapper.class);

        matchHistoryService = new MatchHistoryService(dartsEntryRepository, kickerEntryRepository, matchHistoryEntryMapper);
    }

    @Test
    void getMatchHistory_mergesAndSortsByDateDesc() {
        DartsEntry d1 = new DartsEntry();
        d1.setPlayerName("Max");
        d1.setDartsToFinish(18);
        d1.setCreatedAt(LocalDateTime.of(2025, 12, 24, 12, 30));

        KickerEntry k1 = new KickerEntry();
        k1.setTeamAPlayerNames(List.of("A"));
        k1.setTeamBPlayerNames(List.of("B"));
        k1.setMatchResult(MatchResult.TEAM_A_WIN);
        k1.setCreatedAt(LocalDateTime.of(2025, 12, 25, 9, 0)); // newer than darts

        when(dartsEntryRepository.findLastMatches(any(Pageable.class))).thenReturn(List.of(d1));
        when(kickerEntryRepository.findLastEntries(any(Pageable.class))).thenReturn(List.of(k1));

        MatchHistoryResponse dartsEntry = new MatchHistoryResponse("Darts", "Max", 18, "-", d1.getCreatedAt(), "x");
        MatchHistoryResponse kickerEntry = new MatchHistoryResponse("Kicker", "A vs B", null, "A won", k1.getCreatedAt(), "y");

        when(matchHistoryEntryMapper.toResponse(d1)).thenReturn(dartsEntry);
        when(matchHistoryEntryMapper.toResponse(k1)).thenReturn(kickerEntry);

        List<MatchHistoryResponse> out = matchHistoryService.getMatchHistory();

        assertEquals(2, out.size());
        assertEquals(LocalDateTime.of(2025, 12, 25, 9, 0), out.get(0).date());
        assertEquals(LocalDateTime.of(2025, 12, 24, 12, 30), out.get(1).date());

        verify(dartsEntryRepository).findLastMatches(any(Pageable.class));
        verify(kickerEntryRepository).findLastEntries(any(Pageable.class));
        verify(matchHistoryEntryMapper).toResponse(d1);
        verify(matchHistoryEntryMapper).toResponse(k1);
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, matchHistoryEntryMapper);
    }
}
