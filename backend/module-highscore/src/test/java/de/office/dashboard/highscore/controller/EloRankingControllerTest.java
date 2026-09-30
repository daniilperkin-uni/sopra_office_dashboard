package de.office.dashboard.highscore.controller;

import de.office.dashboard.highscore.service.EloRankingService;
import de.office.dashboard.highscore.service.EloRankingService.PlayerRanking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcTest für den {@link EloRankingController}.
 * <p>
 * Prüft den HTTP-Vertrag (Statuscode und JSON-Form) mit gemocktem Service.
 * </p>
 */
@WebMvcTest(EloRankingController.class)
class EloRankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EloRankingService eloRankingService;

    @Test
    @DisplayName("GET /api/elo-rankings liefert die Rangliste sortiert nach ELO (200)")
    void getRankingsReturnsSortedList() throws Exception {
        when(eloRankingService.getRankings()).thenReturn(List.of(
                new PlayerRanking("Alice", 1043, 3, 1, 2, 2),
                new PlayerRanking("Bob", 980, 1, 2, 0, 1)));

        mockMvc.perform(get("/api/elo-rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playerName").value("Alice"))
                .andExpect(jsonPath("$[0].elo").value(1043))
                .andExpect(jsonPath("$[0].wins").value(3))
                .andExpect(jsonPath("$[0].losses").value(1))
                .andExpect(jsonPath("$[0].currentStreak").value(2))
                .andExpect(jsonPath("$[0].bestStreak").value(2))
                .andExpect(jsonPath("$[1].playerName").value("Bob"))
                .andExpect(jsonPath("$[1].elo").value(980));
    }

    @Test
    @DisplayName("GET /api/elo-rankings liefert ein leeres Array ohne Spieler (200)")
    void getRankingsEmptyList() throws Exception {
        when(eloRankingService.getRankings()).thenReturn(List.of());

        mockMvc.perform(get("/api/elo-rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
