package de.office.dashboard.highscore.controller;

import tools.jackson.databind.ObjectMapper;
import de.office.dashboard.highscore.MatchResult;
import de.office.dashboard.highscore.dto.OverviewResponse;
import de.office.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.office.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.office.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.office.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.office.dashboard.highscore.entity.DartsEntry;
import de.office.dashboard.highscore.entity.KickerEntry;
import de.office.dashboard.highscore.service.HighscoreService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HighscoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class HighscoreControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    HighscoreService highscoreService;

    @Test
    void getOverview_returns200_andBody() throws Exception {
        OverviewResponse response = new OverviewResponse(List.of(), List.of(), List.of());
        Mockito.when(highscoreService.getOverview()).thenReturn(response);

        mvc.perform(get("/api/highscore-overview"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dartsTop5", isA(List.class)))
                .andExpect(jsonPath("$.kickerTop5", isA(List.class)))
                .andExpect(jsonPath("$.matchHistoryEntries", isA(List.class)));

        Mockito.verify(highscoreService).getOverview();
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void getAllDartsMatches_returns200_andList() throws Exception {
        DartsEntry e = new DartsEntry();
        e.setPlayerName("Max Mustermann");
        e.setDartsToFinish(18);
        e.setCreatedAt(LocalDateTime.of(2025, 12, 24, 12, 30));

        Mockito.when(highscoreService.getAllDartsMatches()).thenReturn(List.of(e));

        mvc.perform(get("/api/matches/darts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].playerName", is("Max Mustermann")))
                .andExpect(jsonPath("$[0].dartsToFinish", is(18)));

        Mockito.verify(highscoreService).getAllDartsMatches();
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void deleteDartsMatch_callsService_returns200() throws Exception {
        mvc.perform(delete("/api/matches/darts/{id}", 42L))
                .andExpect(status().isOk());

        Mockito.verify(highscoreService).deleteDartsEntry(42L);
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void getAllKickerMatches_returns200_andList() throws Exception {
        KickerEntry km = new KickerEntry();
        km.setTeamAPlayerNames(List.of("Max", "Dora"));
        km.setTeamBPlayerNames(List.of("Ida", "Hans"));
        km.setMatchResult(MatchResult.TEAM_A_WIN);
        km.setCreatedAt(LocalDateTime.of(2025, 12, 24, 12, 30));

        Mockito.when(highscoreService.getAllKickerMatches()).thenReturn(List.of(km));

        mvc.perform(get("/api/matches/kicker"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].teamAPlayerNames", contains("Max", "Dora")))
                .andExpect(jsonPath("$[0].teamBPlayerNames", contains("Ida", "Hans")))
                .andExpect(jsonPath("$[0].matchResult", is("TEAM_A_WIN")));

        Mockito.verify(highscoreService).getAllKickerMatches();
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void deleteKickerMatch_callsService_returns200() throws Exception {
        mvc.perform(delete("/api/matches/kicker/{id}", 7L))
                .andExpect(status().isOk());

        Mockito.verify(highscoreService).deleteKickerEntry(7L);
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void createKickerEntry_valid_returns201_andBody() throws Exception {
        KickerEntryRequest req = new KickerEntryRequest(
                List.of("Max Mustermann", "Dora Musterfrau"),
                List.of("Ida Oberstein", "Hans Dampf"),
                MatchResult.TEAM_A_WIN
        );

        KickerEntryResponse resp = new KickerEntryResponse(
                2L,
                req.teamAPlayers(),
                req.teamBPlayers(),
                "Team A won",
                "12:30 24.12.2025"
        );

        Mockito.when(highscoreService.createKickerEntry(any(KickerEntryRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/matches/kicker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.teamAPlayers", contains("Max Mustermann", "Dora Musterfrau")))
                .andExpect(jsonPath("$.teamBPlayers", contains("Ida Oberstein", "Hans Dampf")))
                .andExpect(jsonPath("$.matchResult", is("Team A won")))
                .andExpect(jsonPath("$.dateText", is("12:30 24.12.2025")));

        Mockito.verify(highscoreService).createKickerEntry(any(KickerEntryRequest.class));
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void createKickerEntry_invalid_returns400() throws Exception {
        String invalidJson = """
                {
                  "teamAPlayers": [],
                  "teamBPlayers": ["Ida Oberstein"],
                  "matchResult": null
                }
                """;

        mvc.perform(post("/api/matches/kicker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(highscoreService);
    }

    @Test
    void createDartsEntry_valid_returns201_andBody() throws Exception {
        DartsEntryRequest req = new DartsEntryRequest("Max Mustermann", 18);

        DartsEntryResponse resp = new DartsEntryResponse(1L, "Max Mustermann", 18, "12:30 24.12.2025");

        Mockito.when(highscoreService.createDartsEntry(any(DartsEntryRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/matches/darts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.player", is("Max Mustermann")))
                .andExpect(jsonPath("$.points", is(18)))
                .andExpect(jsonPath("$.date", is("12:30 24.12.2025")));

        Mockito.verify(highscoreService).createDartsEntry(any(DartsEntryRequest.class));
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void createDartsEntry_invalid_returns400() throws Exception {
        String invalidJson = """
                {
                  "playerName": "   ",
                  "dartsToFinish": 18
                }
                """;

        mvc.perform(post("/api/matches/darts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(highscoreService);
    }
}
