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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TopServiceTest {

    DartsEntryRepository dartsEntryRepository;
    KickerEntryRepository kickerEntryRepository;
    DartsEntryMapper dartsEntryMapper;
    KickerEntryMapper kickerEntryMapper;

    TopService topService;

    @BeforeEach
    void setUp() {
        dartsEntryRepository = Mockito.mock(DartsEntryRepository.class);
        kickerEntryRepository = Mockito.mock(KickerEntryRepository.class);
        dartsEntryMapper = Mockito.mock(DartsEntryMapper.class);
        kickerEntryMapper = Mockito.mock(KickerEntryMapper.class);

        topService = new TopService(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void getDartsTop3_usesRepository() {
        when(dartsEntryRepository.findTop(any(Pageable.class))).thenReturn(List.of());

        List<DartsPlayerProjection> out = topService.getDartsTop3();

        assertNotNull(out);
        verify(dartsEntryRepository).findTop(any(Pageable.class));
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void getKickerTop3_usesRepository() {
        when(kickerEntryRepository.findKickerTop3Teams()).thenReturn(List.of());

        List<PlayerPointsProjection> out = topService.getKickerTop3();

        assertNotNull(out);
        verify(kickerEntryRepository).findKickerTop3Teams();
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void deleteDartsEntry_callsRepository() {
        topService.deleteDartsEntry(5L);

        verify(dartsEntryRepository).deleteById(5L);
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void deleteKickerEntry_callsRepository() {
        topService.deleteKickerEntry(6L);

        verify(kickerEntryRepository).deleteById(6L);
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void createDartsResult_savesEntity_andReturnsMapperEntry() {
        DartsEntryRequest req = new DartsEntryRequest("Max", 18);

        when(dartsEntryMapper.toResponse(any(DartsEntry.class)))
                .thenReturn(new DartsEntryResponse(1L, "Max", 18, "x"));

        DartsEntryResponse out = topService.createDartsEntry(req);

        assertNotNull(out);
        assertEquals(1L, out.id());
        assertEquals("Max", out.player());
        assertEquals(18, out.points());

        ArgumentCaptor<DartsEntry> captor = ArgumentCaptor.forClass(DartsEntry.class);
        verify(dartsEntryRepository).save(captor.capture());
        DartsEntry saved = captor.getValue();

        assertEquals("Max", saved.getPlayerName());
        assertEquals(18, saved.getDartsToFinish());
        assertNotNull(saved.getCreatedAt());

        verify(dartsEntryMapper).toResponse(any(DartsEntry.class));
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void createKickerResult_validTeams_savesEntity_andReturnsMapperMatch() {
        KickerEntryRequest req = new KickerEntryRequest(
                List.of("Max", "Dora"),
                List.of("Ida", "Hans"),
                MatchResult.TEAM_B_WIN
        );

        when(kickerEntryMapper.toResponse(any(KickerEntry.class)))
                .thenReturn(new KickerEntryResponse(2L, req.teamAPlayers(), req.teamBPlayers(), "Team B won", "x"));

        KickerEntryResponse out = topService.createKickerEntry(req);

        assertNotNull(out);
        assertEquals(2L, out.id());
        assertEquals("Team B won", out.matchResult());

        ArgumentCaptor<KickerEntry> captor = ArgumentCaptor.forClass(KickerEntry.class);
        verify(kickerEntryRepository).save(captor.capture());
        KickerEntry saved = captor.getValue();

        assertEquals(req.teamAPlayers(), saved.getTeamAPlayerNames());
        assertEquals(req.teamBPlayers(), saved.getTeamBPlayerNames());
        assertEquals(MatchResult.TEAM_B_WIN, saved.getMatchResult());
        assertNotNull(saved.getCreatedAt());

        verify(kickerEntryMapper).toResponse(any(KickerEntry.class));
        verifyNoMoreInteractions(dartsEntryRepository, kickerEntryRepository, dartsEntryMapper, kickerEntryMapper);
    }

    @Test
    void createKickerEntry_rejectsWrongTeamSize() {
        KickerEntryRequest req = new KickerEntryRequest(
                List.of(),
                List.of("Ida"),
                MatchResult.TEAM_A_WIN
        );

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> topService.createKickerEntry(req));
        assertTrue(ex.getMessage().contains("Teams must be 1 or 2 players"));

        verifyNoInteractions(kickerEntryRepository, kickerEntryMapper);
        verifyNoInteractions(dartsEntryRepository, dartsEntryMapper);
    }

    @Test
    void createKickerEntry_rejectsDuplicatePlayersAcrossTeams() {
        KickerEntryRequest req = new KickerEntryRequest(
                List.of("Max"),
                List.of("Max"),
                MatchResult.TEAM_A_WIN
        );

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> topService.createKickerEntry(req));
        assertTrue(ex.getMessage().contains("Duplicate player"));

        verifyNoInteractions(kickerEntryRepository, kickerEntryMapper);
        verifyNoInteractions(dartsEntryRepository, dartsEntryMapper);
    }
}
