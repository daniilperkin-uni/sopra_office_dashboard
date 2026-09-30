package de.office.dashboard.highscore.service;

import de.office.dashboard.highscore.MatchResult;
import de.office.dashboard.highscore.dto.OverviewResponse;
import de.office.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.office.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.office.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.office.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.office.dashboard.highscore.dto.matchhistory.response.MatchHistoryResponse;
import de.office.dashboard.highscore.entity.DartsEntry;
import de.office.dashboard.highscore.entity.KickerEntry;
import de.office.dashboard.highscore.projection.DartsPlayerProjection;
import de.office.dashboard.highscore.projection.PlayerPointsProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HighscoreServiceTest {

    TopService topService;
    MatchHistoryService matchHistoryService;
    HighscoreService highscoreService;

    @BeforeEach
    void setUp() {
        topService = Mockito.mock(TopService.class);
        matchHistoryService = Mockito.mock(MatchHistoryService.class);
        highscoreService = new HighscoreService(topService, matchHistoryService);
    }

    @Test
    void getOverview_delegatesToTopAndHistory() {
        List<DartsPlayerProjection> dartsTop = List.of();

        List<PlayerPointsProjection> kickerTop = List.of();

        List<MatchHistoryResponse> history = List.of();

        when(topService.getDartsTop3()).thenReturn(dartsTop);
        when(topService.getKickerTop3()).thenReturn(kickerTop);
        when(matchHistoryService.getMatchHistory()).thenReturn(history);

        OverviewResponse res = highscoreService.getOverview();

        assertNotNull(res);
        assertSame(dartsTop, res.dartsTop5());
        assertSame(kickerTop, res.kickerTop5());
        assertSame(history, res.matchHistoryEntries());

        verify(topService).getDartsTop3();
        verify(topService).getKickerTop3();
        verify(matchHistoryService).getMatchHistory();
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void getAllDartsMatches_delegates() {
        when(topService.getAllDartsMatches()).thenReturn(List.of(new DartsEntry()));

        assertEquals(1, highscoreService.getAllDartsMatches().size());

        verify(topService).getAllDartsMatches();
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void deleteDartsEntry_delegates() {
        highscoreService.deleteDartsEntry(10L);

        verify(topService).deleteDartsEntry(10L);
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void getAllKickerMatches_delegates() {
        when(topService.getAllKickerMatches()).thenReturn(List.of(new KickerEntry()));

        assertEquals(1, highscoreService.getAllKickerMatches().size());

        verify(topService).getAllKickerMatches();
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void deleteKickerEntry_delegates() {
        highscoreService.deleteKickerEntry(11L);

        verify(topService).deleteKickerEntry(11L);
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void createDartsEntry_delegates() {
        DartsEntryRequest req = new DartsEntryRequest("Max", 18);

        when(topService.createDartsEntry(any(DartsEntryRequest.class)))
                .thenReturn(new DartsEntryResponse(1L, "Max", 18, "x"));

        DartsEntryResponse out = highscoreService.createDartsEntry(req);

        assertEquals(1L, out.id());
        assertEquals("Max", out.player());
        assertEquals(18, out.points());

        verify(topService).createDartsEntry(eq(req));
        verifyNoMoreInteractions(topService, matchHistoryService);
    }

    @Test
    void createKickerEntry_delegates() {
        KickerEntryRequest req = new KickerEntryRequest(List.of("A"), List.of("B"), MatchResult.TEAM_A_WIN);

        when(topService.createKickerEntry(any(KickerEntryRequest.class)))
                .thenReturn(new KickerEntryResponse(2L, List.of("A"), List.of("B"), "Team A won", "x"));

        KickerEntryResponse out = highscoreService.createKickerEntry(req);

        assertEquals(2L, out.id());
        assertEquals("Team A won", out.matchResult());

        verify(topService).createKickerEntry(eq(req));
        verifyNoMoreInteractions(topService, matchHistoryService);
    }
}
